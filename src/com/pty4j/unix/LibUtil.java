package com.pty4j.unix;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import com.pty4j.util.PtyUtil;

public class LibUtil {

  public static int login_tty(int __fd) {
    try {
      return (int) Helper.downcallHandle("login_tty", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public final static class Helper {

    private static final SymbolLookup SYMBOL_LOOKUP;

    static {
      SymbolLookup loaderLookup = SymbolLookup.libraryLookup(PtyUtil.resolveNativeFile("win-helper.dll").toPath(), Arena.global());
      SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LibC.LINKER.defaultLookup().find(name));
    }

    static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
      return SYMBOL_LOOKUP.find(name).map(addr -> LibC.LINKER.downcallHandle(addr, fdesc)).orElse(null);
    }
  }

}
