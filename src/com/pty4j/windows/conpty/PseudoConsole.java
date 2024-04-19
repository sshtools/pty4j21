package com.pty4j.windows.conpty;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.jetbrains.annotations.NotNull;

import com.pty4j.Native;
import com.pty4j.WinSize;
import com.pty4j.windows.Kernel32;

final class PseudoConsole {

  private final MemorySegment hpc;
  private WinSize myLastWinSize;
  private boolean myClosed = false;
  private Arena mem;

  private static MemorySegment getSizeCoords(Arena mem, @NotNull WinSize size) {
	  MemorySegment sizeCoords = mem.allocate(ConPtyLibrary._COORD.layout());
	  ConPtyLibrary._COORD.X(sizeCoords, (short) size.getColumns());
	  ConPtyLibrary._COORD.Y(sizeCoords, (short) size.getRows());
    return sizeCoords;
  }

  public PseudoConsole(WinSize size, MemorySegment input, MemorySegment output) throws LastErrorExceptionEx {
	mem = Arena.ofAuto();
	MemorySegment hpcByReference = mem.allocate(Native.C_POINTER);
    if (ConPtyLibrary.CreatePseudoConsole(getSizeCoords(mem, size), input, output, 0, hpcByReference) != Kernel32.S_OK) {
      throw new LastErrorExceptionEx("CreatePseudoConsole");
    }
    hpc = hpcByReference.get(Native.C_POINTER, 0);
    myLastWinSize = size;
  }

  public MemorySegment getHandle() {
    return hpc;
  }

  public void resize(@NotNull WinSize newSize) throws IOException {
    if (ConPtyLibrary.ResizePseudoConsole(hpc, getSizeCoords(mem, newSize)) != Kernel32.S_OK) {
      throw new LastErrorExceptionEx("ResizePseudoConsole");
    }
    myLastWinSize = newSize;
  }

  public @NotNull WinSize getWinSize() throws IOException {
    if (myClosed) {
      throw new IOException(WinConPtyProcess.class.getName() + ": unable to get window size for closed PseudoConsole");
    }
    return myLastWinSize;
  }

  public void close() {
    if (!myClosed) {
      myClosed = true;
      ConPtyLibrary.ClosePseudoConsole(hpc);
    }
  }
}
