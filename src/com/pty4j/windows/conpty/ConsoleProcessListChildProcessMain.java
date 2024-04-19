package com.pty4j.windows.conpty;

import static com.pty4j.Native.err;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import com.pty4j.windows.Kernel32;

public class ConsoleProcessListChildProcessMain {
  static final String PREFIX = "Process list count: ";
  static final String SUFFIX = " attached to the console";

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("single argument expected: pid");
      return;
    }
    int pid;
    try {
      pid = Integer.parseInt(args[0]);
    } catch (NumberFormatException e) {
      System.err.println("Cannot parse pid from " + args[0]);
      return;
    }
    if (err(Kernel32.FreeConsole())) {
      System.err.println(LastErrorExceptionEx.getErrorMessage("FreeConsole"));
      return;
    }
    if (err(Kernel32.AttachConsole(pid))) {
      System.err.println(LastErrorExceptionEx.getErrorMessage("AttachConsole"));
      return;
    }
    int MAX_COUNT = 64;

    try (var offHeap = Arena.ofConfined()) {
    	MemorySegment buffer = offHeap.allocate(ValueLayout.JAVA_INT.byteSize() * MAX_COUNT);
    	int count = Kernel32.GetConsoleProcessList(buffer, MAX_COUNT);
        if (count == 0) {
          System.err.println(LastErrorExceptionEx.getErrorMessage("GetConsoleProcessList"));
          return;
        }
        System.out.println(PREFIX + count + SUFFIX);
    }
  }
}
