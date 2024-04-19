package com.pty4j.windows.winpty;

import static com.pty4j.Native.err;
import static com.pty4j.Native.ok;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.locks.ReentrantLock;

import com.pty4j.Native;
import com.pty4j.windows.Kernel32;
import com.pty4j.windows.Kernel32.OVERLAPPED;

public class NamedPipe {
  private MemorySegment myHandle;
  boolean myCloseHandleOnFinalize;

  private MemorySegment shutdownEvent;
  private volatile boolean shutdownFlag = false;
  private volatile boolean myFinalizedFlag = false;

  private ReentrantLock readLock = new ReentrantLock();
  private ReentrantLock writeLock = new ReentrantLock();

  private MemorySegment readBuffer;
  private MemorySegment writeBuffer;

  private MemorySegment readEvent;
  private MemorySegment writeEvent;

  private MemorySegment readWaitHandles;
  private MemorySegment writeWaitHandles;

  private final MemorySegment readActual;
  private final MemorySegment writeActual;
  private final MemorySegment peekActual;

  private MemorySegment readOver;
  private MemorySegment writeOver;
  
  private final Arena mem;

  /**
   * The NamedPipe object closes the given handle when it is closed.  If you
   * do not own the handle, call markClosed instead of close, or call the Win32
   * DuplicateHandle API to get a new handle.
   */
  public NamedPipe(MemorySegment handle, boolean closeHandleOnFinalize) {
	mem = Arena.ofShared();
	readOver = mem.allocate(Kernel32.OVERLAPPED.layout());
	writeOver = mem.allocate(Kernel32.OVERLAPPED.layout());
	readActual = mem.allocate(ValueLayout.JAVA_INT);
	writeActual = mem.allocate(ValueLayout.JAVA_INT);
	peekActual = mem.allocate(ValueLayout.JAVA_INT);
	readBuffer = mem.allocate(16 * 1024);
	writeBuffer = mem.allocate(16 * 1024);
    myHandle = handle;
    myCloseHandleOnFinalize = closeHandleOnFinalize;
    shutdownEvent = Kernel32.CreateEvent(MemorySegment.NULL, Native.TRUE, Native.FALSE, MemorySegment.NULL);
    readEvent = Kernel32.CreateEvent(MemorySegment.NULL, Native.TRUE, Native.FALSE, MemorySegment.NULL);
    writeEvent = Kernel32.CreateEvent(MemorySegment.NULL, Native.TRUE, Native.FALSE, MemorySegment.NULL);
    
    readWaitHandles = mem.allocateArray(Native.C_POINTER, 2);
    readWaitHandles.setAtIndex(Native.C_POINTER, 0, readEvent);
    readWaitHandles.setAtIndex(Native.C_POINTER, 1, shutdownEvent);
    
    writeWaitHandles = mem.allocateArray(Native.C_POINTER, 2);
    writeWaitHandles.setAtIndex(Native.C_POINTER, 0, writeEvent);
    writeWaitHandles.setAtIndex(Native.C_POINTER, 1, shutdownEvent);
  }

  public static NamedPipe connectToServer(String name, int desiredAccess) throws IOException {
	try(var mem = Arena.ofConfined()) {
	    MemorySegment handle = Kernel32.CreateFileW(
	        Native.toWideString(name, mem), desiredAccess, 0, MemorySegment.NULL, Kernel32.OPEN_EXISTING, 0, MemorySegment.NULL);
	    if (handle.equals(Kernel32.INVALID_HANDLE_VALUE)) {
	      throw new IOException("Error connecting to pipe '" + name + "': " + Kernel32.GetLastError());
	    }
	    return new NamedPipe(handle, true);
	}
  }

  /**
   * Returns -1 on any kind of error, including a pipe that isn't connected or
   * a NamedPipe instance that has been closed.
   */
  public int read(byte[] buf, int off, int len) {
    if (buf == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || len > buf.length - off) {
      throw new IndexOutOfBoundsException();
    }
    readLock.lock();
    try {
      if (shutdownFlag) {
        return -1;
      }
      if (len == 0) {
        return 0;
      }
      if (readBuffer.byteSize() < len) {
    	len = (int)readBuffer.byteSize();
      }
      OVERLAPPED.hEvent(readOver, readEvent);
      readActual.set(ValueLayout.JAVA_INT, 0, 0);
      boolean success = ok(Kernel32.ReadFile(myHandle, readBuffer, len, readActual, readOver)) ;
      if (!success && Kernel32.GetLastError() == Kernel32.ERROR_IO_PENDING) {
        int waitRet = Kernel32.WaitForMultipleObjects(2, readWaitHandles, Native.FALSE, Kernel32.INFINITE);
        if (waitRet != Kernel32.WAIT_OBJECT_0) {
        	Kernel32.CancelIo(myHandle);
        }
        success = ok(Kernel32.GetOverlappedResult(myHandle, readOver, readActual, Native.TRUE));
      }
      int actual = readActual.get(ValueLayout.JAVA_INT, 0);
      if (!success || actual <= 0) {
        return -1;
      }
      readBuffer.asByteBuffer().get(0, buf, off, actual);
      return actual;
    } finally {
      readLock.unlock();
    }
  }

  /**
   * This function ignores I/O errors.
   */
  public void write(byte[] buf, int off, int len) {
    if (buf == null) {
      throw new NullPointerException();
    }
    if (off < 0 || len < 0 || len > buf.length - off) {
      throw new IndexOutOfBoundsException();
    }
    writeLock.lock();
    try {
      if (shutdownFlag) {
        return;
      }
      if (len == 0) {
        return;
      }
      if (writeBuffer.byteSize() < len) {
    	len = (int)writeBuffer.byteSize();
      }
      writeBuffer.asByteBuffer().put(0, buf, off, len);
      OVERLAPPED.hEvent(writeOver, writeEvent);
      writeActual.set(ValueLayout.JAVA_INT, 0, 0);
      boolean success = ok(Kernel32.WriteFile(myHandle, writeBuffer, len, writeActual, writeOver));
      if (!success && Kernel32.GetLastError() == Kernel32.ERROR_IO_PENDING) {
        int waitRet = Kernel32.WaitForMultipleObjects(2, writeWaitHandles, Native.FALSE, Kernel32.INFINITE);
        if (waitRet != Kernel32.WAIT_OBJECT_0) {
          Kernel32.CancelIo(myHandle);
        }
        Kernel32.GetOverlappedResult(myHandle, writeOver, writeActual, Native.TRUE);
      }
    } finally {
      writeLock.unlock();
    }
  }

  public int available() throws IOException {
    readLock.lock();
    try {
      if (shutdownFlag) {
        return -1;
      }
      peekActual.set(ValueLayout.JAVA_INT, 0, 0);
      if (err(Kernel32.PeekNamedPipe(myHandle, MemorySegment.NULL, 0, MemorySegment.NULL, peekActual, MemorySegment.NULL))) {
        throw new IOException("PeekNamedPipe failed");
      }
      return peekActual.get(ValueLayout.JAVA_INT, 0);
    } finally {
      readLock.unlock();
    }
  }

  /**
   * Like close(), but leave the pipe handle itself alone.
   */
  public synchronized void markClosed() {
    closeImpl();
  }

  /**
   * Shut down the NamedPipe cleanly and quickly.  Use an event to abort any
   * pending I/O, then acquire the locks to ensure that the I/O has ended.
   * Once everything has stopped, close all the native handles.
   *
   * Mark the function synchronized to ensure that a later call cannot return
   * earlier.
   */
  public synchronized void close() throws IOException {
	try {
      if (!closeImpl()) {
        return;
      }
      if (err(Kernel32.CloseHandle(myHandle))) {
        throw new IOException("Close error:" + Kernel32.GetLastError());
      }
	} finally {
	  mem.close();
	}
  }

  private synchronized boolean closeImpl() {
    if (shutdownFlag) {
      // If shutdownFlag is already set, then the handles are already closed.
      return false;
    }
    shutdownFlag = true;
    Kernel32.SetEvent(shutdownEvent);
    if (!myFinalizedFlag) {
      readLock.lock();
      writeLock.lock();
      writeLock.unlock();
      readLock.unlock();
    }
    Kernel32.CloseHandle(shutdownEvent);
    Kernel32.CloseHandle(readEvent);
    Kernel32.CloseHandle(writeEvent);
    return true;
  }

  @Override
  protected synchronized void finalize() throws Throwable {
    // Once the object begins finalization, we can't assume much about other
    // objects referenced by this object, because they may have already been
    // finalized.  We can assume that there are no other references to this
    // object, though, except from objects that are also being finalized.  When
    // this flag is set, avoid using the ReentrantLock objects.
    myFinalizedFlag = true;
    if (myCloseHandleOnFinalize) {
      close();
    }
    super.finalize();
  }
}
