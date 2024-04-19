package com.pty4j.windows.conpty;

import static com.pty4j.Native.err;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.pty4j.windows.Kernel32;

public class WinHandleOutputStream extends OutputStream {
  private final MemorySegment myWritePipe;
  private volatile boolean myClosed;

  public WinHandleOutputStream(@NotNull MemorySegment writePipe) {
    myWritePipe = writePipe;
  }

  @Override
  public void write(int b) throws IOException {
    write(new byte[]{(byte) b}, 0, 1);
  }

  @Override
  public void write(byte @NotNull [] b, int off, int len) throws IOException {
    Objects.checkFromIndexSize(off, len, b.length);
    if (len == 0) {
      return;
    }
    if (myClosed) {
      throw new IOException("Closed stdout");
    }
    try(Arena mem = Arena.ofConfined()) {
    	MemorySegment buffer = mem.allocate(len);
    	buffer.asByteBuffer().put(b, off, len);
	    MemorySegment lpNumberOfBytesWritten = mem.allocate(ValueLayout.JAVA_INT);
	    if (err(Kernel32.WriteFile(myWritePipe, buffer, len, lpNumberOfBytesWritten, MemorySegment.NULL))) {
	      throw new LastErrorExceptionEx("WriteFile stdout");
	    }
    }
  }

  @Override
  public void close() throws IOException {
    if (!myClosed) {
      myClosed = true;
      if (err(Kernel32.CloseHandle(myWritePipe))) {
        throw new LastErrorExceptionEx("CloseHandle stdout");
      }
    }
  }
}
