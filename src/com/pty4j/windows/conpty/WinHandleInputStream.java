package com.pty4j.windows.conpty;

import static com.pty4j.Native.err;
import static com.pty4j.Native.ok;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;

import com.pty4j.PtyInputStream;
import com.pty4j.windows.Kernel32;

class WinHandleInputStream extends PtyInputStream {

  private static final Logger LOG = System.getLogger(WinHandleInputStream.class.getName());

  private final MemorySegment myReadPipe;
  private volatile boolean myClosed;
  private final ReentrantLock myLock = new ReentrantLock();
  private int myReadCount = 0; // guarded by myLock
  private final Condition myReadCountChanged = myLock.newCondition();

  public WinHandleInputStream(@NotNull MemorySegment readPipe) {
    myReadPipe = readPipe;
  }

  @Override
  public int read() throws IOException {
    byte[] buf = new byte[1];
    int readBytes = read(buf, 0, 1);
    return readBytes == 1 ? buf[0] : -1;
  }

  @Override
  public int read(byte @NotNull [] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    myLock.lock();
    try {
      myReadCount++;
      myReadCountChanged.signalAll();
    }
    finally {
      myLock.unlock();
    }
    if (len == 0) {
      return 0;
    }
    if (myClosed) {
      throw new IOException("Closed stdin");
    }
    try(Arena mem = Arena.ofConfined()) {
    	MemorySegment buffer = mem.allocate(len);
	    MemorySegment lpNumberOfBytesRead = mem.allocate(ValueLayout.JAVA_INT);
	    boolean result = ok(Kernel32.ReadFile(myReadPipe, buffer, len, lpNumberOfBytesRead, MemorySegment.NULL));
	    if (!result) {
	      int lastError = Kernel32.GetLastError();
	      if (lastError == Kernel32.ERROR_BROKEN_PIPE) {
	        // https://docs.microsoft.com/en-us/windows/win32/api/fileapi/nf-fileapi-readfile
	        // If an anonymous pipe is being used and the write handle has been closed,
	        // when ReadFile attempts to read using the pipe's corresponding read handle,
	        // the function returns FALSE and GetLastError returns ERROR_BROKEN_PIPE.
	        return -1;
	      }
	      throw new LastErrorExceptionEx("ReadFile stdin", lastError);
	    }
	    int bytesRead = lpNumberOfBytesRead.get(ValueLayout.JAVA_INT, 0);
	    if (bytesRead == 0) {
	      // If lpOverlapped is NULL, then when a synchronous read operation reaches the end of a file,
	      // ReadFile returns TRUE and sets *lpNumberOfBytesRead to zero.
	      return -1;
	    }
	    buffer.asByteBuffer().get(b, off, len);
	    return bytesRead;
    }
  }

  @Override
  public void close() throws IOException {
    if (!myClosed) {
      myClosed = true;
      if (err(Kernel32.CloseHandle(myReadPipe))) {
        throw new LastErrorExceptionEx("CloseHandle stdin");
      }
    }
  }

  void awaitAvailableOutputIsRead() {
    myLock.lock();
    try {
      if (myReadCount == 0 && !myReadCountChanged.await(2000, TimeUnit.MILLISECONDS)) {
        LOG.log(Level.WARNING, "Nobody called {0}.read after the process creation!", WinHandleInputStream.class.getName());
        return;
      }
      long start = System.currentTimeMillis();
      int oldReadCount;
      do {
        oldReadCount = myReadCount;
      } while (myReadCountChanged.await(100, TimeUnit.MILLISECONDS) &&
          oldReadCount < myReadCount &&
          System.currentTimeMillis() - start < 2000);
    } catch (InterruptedException ignored) {
    } finally {
      myLock.unlock();
    }
  }
}
