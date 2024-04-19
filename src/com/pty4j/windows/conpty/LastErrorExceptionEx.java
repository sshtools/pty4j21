package com.pty4j.windows.conpty;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import org.jetbrains.annotations.NotNull;

import com.pty4j.Native;
import com.pty4j.windows.Kernel32;

@SuppressWarnings("serial")
public class LastErrorExceptionEx extends IOException {

  public LastErrorExceptionEx(@NotNull String action) {
    super(getErrorMessage(action));
  }

  public LastErrorExceptionEx(@NotNull String action, int lastError) {
    super(getErrorMessage(action, lastError));
  }

  public static @NotNull String getErrorMessage(@NotNull String action) {
    return getErrorMessage(action, Kernel32.GetLastError());
  }

  private static @NotNull String getErrorMessage(@NotNull String action, int lastError) {
    return action + " failed: GetLastError() returned " + lastError + ": " + formatMessage(lastError);
  }
  
  public static String formatMessage(int code) {
	  try(Arena mem = Arena.ofConfined()) {
		  MemorySegment buffer = mem.allocate(Native.C_POINTER);
	      int nLen = Kernel32.FormatMessageW(
	    		  Kernel32.FORMAT_MESSAGE_ALLOCATE_BUFFER
	              | Kernel32.FORMAT_MESSAGE_FROM_SYSTEM
	              | Kernel32.FORMAT_MESSAGE_IGNORE_INSERTS,
	              MemorySegment.NULL,
	              code,
	              0,
	              buffer, 0, MemorySegment.NULL);
	      if (nLen == 0) {
	          return getErrorMessage("formatMessage");
	      }
	
    	  return Native.toJavaString(buffer.get(Native.C_POINTER, 0), 1024).trim();
	  }
  }
}
