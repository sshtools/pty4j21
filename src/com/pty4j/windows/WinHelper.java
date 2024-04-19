package com.pty4j.windows;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;

import com.pty4j.unix.LibC;
import com.pty4j.unix.LibC.LibCHelper;
import com.pty4j.util.PtyUtil;

public final class WinHelper {

	public static String getCurrentDirectory(long processId) {
		try (var offHeap = Arena.ofConfined()) {
			var errorMessagePtr = offHeap.allocate(ADDRESS);
			var currentDirectory = (MemorySegment) LibCHelper
					.downcallHandle("getCurrentDirectory",
							FunctionDescriptor.of(LibCHelper.POINTER, JAVA_LONG, ADDRESS))
					.invokeExact(processId, errorMessagePtr);
			var err = errorMessagePtr.getUtf8String(0);
			if (currentDirectory != null) {
				if (err.length() > 0) {
					throw new IOException("Unexpected error message: " + err);
				}
				return currentDirectory.getUtf8String(0);
			}
			if (err.length() == 0) {
				throw new IOException("getCurrentDirectory failed without error message");
			}
			throw new IOException("getCurrentDirectory failed: " + err);
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

	private WinHelper() {
	}

}
