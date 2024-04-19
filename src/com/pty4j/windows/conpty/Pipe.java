package com.pty4j.windows.conpty;

import static com.pty4j.Native.err;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import com.pty4j.Native;
import com.pty4j.windows.Kernel32;

final class Pipe {

  private final MemorySegment readPipe;
  private final MemorySegment writePipe;
  private final Arena mem;

  public Pipe() throws IOException {
	  
	mem = Arena.ofAuto();
    MemorySegment readPipeRef = mem.allocate(Native.C_POINTER);
    MemorySegment writePipeRef = mem.allocate(Native.C_POINTER);
    if (err(Kernel32.CreatePipe(readPipeRef, writePipeRef, MemorySegment.NULL, 0))) {
      throw new LastErrorExceptionEx("CreatePipe");
    }
    readPipe = readPipeRef.get(Native.C_POINTER, 0);
    writePipe = writePipeRef.get(Native.C_POINTER, 0);
  }

  public MemorySegment getReadPipe() {
    return readPipe;
  }

  public MemorySegment getWritePipe() {
    return writePipe;
  }
}
