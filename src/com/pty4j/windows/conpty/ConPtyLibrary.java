package com.pty4j.windows.conpty;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout.OfShort;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.pty4j.Native;
import com.pty4j.unix.LibC;
import com.pty4j.util.PtyUtil;
import com.pty4j.windows.Kernel32;

public final class ConPtyLibrary {

    private final static String CONPTY = "conpty.dll";
    private final static String DISABLE_BUNDLED_CONPTY_PROP_NAME = "com.pty4j.windows.disable.bundled.conpty";

	private static class ClosePseudoConsole {
		public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("ClosePseudoConsole", DESC);
	}

	public static void ClosePseudoConsole(MemorySegment hPC) {
		try {
			ClosePseudoConsole.HANDLE.invokeExact(hPC);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreatePseudoConsole {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, _COORD.layout(), Native.C_POINTER,
				Native.C_POINTER, JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreatePseudoConsole", DESC);
	}

	public static int CreatePseudoConsole(MemorySegment size, MemorySegment hInput, MemorySegment hOutput, int dwFlags,
			MemorySegment phPC) {
		try {
			return (int) CreatePseudoConsole.HANDLE.invokeExact(size, hInput, hOutput, dwFlags, phPC);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class ResizePseudoConsole {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				_COORD.layout());

		public static final MethodHandle HANDLE = Helper.downcallHandle("ResizePseudoConsole", DESC);
	}

	public static int ResizePseudoConsole(MemorySegment hPC, MemorySegment size) {
		try {
			return (int) ResizePseudoConsole.HANDLE.invokeExact(hPC, size);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	public final static class Helper {

		private static final SymbolLookup SYMBOL_LOOKUP;

		static {
			SymbolLookup loaderLookup;
			var bundledConptyDll = PtyUtil.resolveNativeFile(CONPTY).toPath();
			if (!Boolean.getBoolean(DISABLE_BUNDLED_CONPTY_PROP_NAME) && estimateOpenConsoleCommandLineLength(bundledConptyDll) < Kernel32.MAX_PATH) {
				loaderLookup = Native.load(bundledConptyDll, "kernel32", Arena.global());
			}
			else {
				loaderLookup = Native.load("kernel32", Arena.global());
			
			}
			SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LibC.LINKER.defaultLookup().find(name));
		}

		static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
			return SYMBOL_LOOKUP.find(name).map(addr -> LibC.LINKER.downcallHandle(addr, fdesc)).orElseThrow(() -> new IllegalArgumentException("No " + name));
		}
	}
	
	private final static int estimateOpenConsoleCommandLineLength(Path conptyDll) {
	      var parentDirPath = conptyDll.getParent().toAbsolutePath().toString();
	      // Estimate OpenConsole.exe command line length by looking at how it's constructed:
	      // https://github.com/microsoft/terminal/blob/a38388615e299658072f906578acd60e976fe787/src/winconpty/winconpty.cpp#L142
	      var commandLine = "\"" + parentDirPath + "\\OpenConsole.exe\" --headless --width 120 --height 100 --signal 0x950 --server 0x958";
	      var reservedOptions = "--resizeQuirk --passthrough "; // unused now, works as safety gap
	      return commandLine.length() + reservedOptions.length();
	}
	
	private ConPtyLibrary() {
	}


	public final static class _COORD {

		_COORD() {
			// Should not be called directly
		}

		private static final GroupLayout $LAYOUT = MemoryLayout
				.structLayout(JAVA_SHORT.withName("X"), JAVA_SHORT.withName("Y")).withName("_COORD");

		/**
		 * The layout of this struct
		 */
		public static final GroupLayout layout() {
			return $LAYOUT;
		}

		private static final OfShort X$LAYOUT = (OfShort) $LAYOUT.select(groupElement("X"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * SHORT X
		 * }
		 */
		public static final OfShort X$layout() {
			return X$LAYOUT;
		}

		private static final long X$OFFSET = 0;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * SHORT X
		 * }
		 */
		public static final long X$offset() {
			return X$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * SHORT X
		 * }
		 */
		public static short X(MemorySegment struct) {
			return struct.get(X$LAYOUT, X$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * SHORT X
		 * }
		 */
		public static void X(MemorySegment struct, short fieldValue) {
			struct.set(X$LAYOUT, X$OFFSET, fieldValue);
		}

		private static final OfShort Y$LAYOUT = (OfShort) $LAYOUT.select(groupElement("Y"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * SHORT Y
		 * }
		 */
		public static final OfShort Y$layout() {
			return Y$LAYOUT;
		}

		private static final long Y$OFFSET = 2;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * SHORT Y
		 * }
		 */
		public static final long Y$offset() {
			return Y$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * SHORT Y
		 * }
		 */
		public static short Y(MemorySegment struct) {
			return struct.get(Y$LAYOUT, Y$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * SHORT Y
		 * }
		 */
		public static void Y(MemorySegment struct, short fieldValue) {
			struct.set(Y$LAYOUT, Y$OFFSET, fieldValue);
		}

		/**
		 * Obtains a slice of {@code arrayParam} which selects the array element at
		 * {@code index}. The returned segment has address
		 * {@code arrayParam.address() + index * layout().byteSize()}
		 */
		public static MemorySegment asSlice(MemorySegment array, long index) {
			return array.asSlice(layout().byteSize() * index);
		}

		/**
		 * The size (in bytes) of this struct
		 */
		public static long sizeof() {
			return layout().byteSize();
		}

		/**
		 * Allocate a segment of size {@code layout().byteSize()} using
		 * {@code allocator}
		 */
		public static MemorySegment allocate(SegmentAllocator allocator) {
			return allocator.allocate(layout());
		}

		/**
		 * Allocate an array of size {@code elementCount} using {@code allocator}. The
		 * returned segment has size {@code elementCount * layout().byteSize()}.
		 */
		public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
			return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
			return reinterpret(addr, 1, arena, cleanup);
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * elementCount * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena,
				Consumer<MemorySegment> cleanup) {
			return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
		}
	}


	
	/**
	 * {@snippet lang=c :
	 * struct _CONSOLE_SCREEN_BUFFER_INFO {
	 *     COORD dwSize;
	 *     COORD dwCursorPosition;
	 *     WORD wAttributes;
	 *     SMALL_RECT srWindow;
	 *     COORD dwMaximumWindowSize;
	 * }
	 * }
	 */
	public final static class _CONSOLE_SCREEN_BUFFER_INFO {

	    _CONSOLE_SCREEN_BUFFER_INFO() {
	        // Should not be called directly
	    }

	    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
	        _COORD.layout().withName("dwSize"),
	        _COORD.layout().withName("dwCursorPosition"),
	        JAVA_SHORT.withName("wAttributes"),
	        _SMALL_RECT.layout().withName("srWindow"),
	        _COORD.layout().withName("dwMaximumWindowSize")
	    ).withName("_CONSOLE_SCREEN_BUFFER_INFO");

	    /**
	     * The layout of this struct
	     */
	    public static final GroupLayout layout() {
	        return $LAYOUT;
	    }

	    private static final GroupLayout dwSize$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("dwSize"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * COORD dwSize
	     * }
	     */
	    public static final GroupLayout dwSize$layout() {
	        return dwSize$LAYOUT;
	    }

	    private static final long dwSize$OFFSET = 0;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * COORD dwSize
	     * }
	     */
	    public static final long dwSize$offset() {
	        return dwSize$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * COORD dwSize
	     * }
	     */
	    public static MemorySegment dwSize(MemorySegment struct) {
	        return struct.asSlice(dwSize$OFFSET, dwSize$LAYOUT.byteSize());
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * COORD dwSize
	     * }
	     */
	    public static void dwSize(MemorySegment struct, MemorySegment fieldValue) {
	        MemorySegment.copy(fieldValue, 0L, struct, dwSize$OFFSET, dwSize$LAYOUT.byteSize());
	    }

	    private static final GroupLayout dwCursorPosition$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("dwCursorPosition"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * COORD dwCursorPosition
	     * }
	     */
	    public static final GroupLayout dwCursorPosition$layout() {
	        return dwCursorPosition$LAYOUT;
	    }

	    private static final long dwCursorPosition$OFFSET = 4;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * COORD dwCursorPosition
	     * }
	     */
	    public static final long dwCursorPosition$offset() {
	        return dwCursorPosition$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * COORD dwCursorPosition
	     * }
	     */
	    public static MemorySegment dwCursorPosition(MemorySegment struct) {
	        return struct.asSlice(dwCursorPosition$OFFSET, dwCursorPosition$LAYOUT.byteSize());
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * COORD dwCursorPosition
	     * }
	     */
	    public static void dwCursorPosition(MemorySegment struct, MemorySegment fieldValue) {
	        MemorySegment.copy(fieldValue, 0L, struct, dwCursorPosition$OFFSET, dwCursorPosition$LAYOUT.byteSize());
	    }

	    private static final OfShort wAttributes$LAYOUT = (OfShort)$LAYOUT.select(groupElement("wAttributes"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * WORD wAttributes
	     * }
	     */
	    public static final OfShort wAttributes$layout() {
	        return wAttributes$LAYOUT;
	    }

	    private static final long wAttributes$OFFSET = 8;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * WORD wAttributes
	     * }
	     */
	    public static final long wAttributes$offset() {
	        return wAttributes$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * WORD wAttributes
	     * }
	     */
	    public static short wAttributes(MemorySegment struct) {
	        return struct.get(wAttributes$LAYOUT, wAttributes$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * WORD wAttributes
	     * }
	     */
	    public static void wAttributes(MemorySegment struct, short fieldValue) {
	        struct.set(wAttributes$LAYOUT, wAttributes$OFFSET, fieldValue);
	    }

	    private static final GroupLayout srWindow$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("srWindow"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * SMALL_RECT srWindow
	     * }
	     */
	    public static final GroupLayout srWindow$layout() {
	        return srWindow$LAYOUT;
	    }

	    private static final long srWindow$OFFSET = 10;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * SMALL_RECT srWindow
	     * }
	     */
	    public static final long srWindow$offset() {
	        return srWindow$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * SMALL_RECT srWindow
	     * }
	     */
	    public static MemorySegment srWindow(MemorySegment struct) {
	        return struct.asSlice(srWindow$OFFSET, srWindow$LAYOUT.byteSize());
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * SMALL_RECT srWindow
	     * }
	     */
	    public static void srWindow(MemorySegment struct, MemorySegment fieldValue) {
	        MemorySegment.copy(fieldValue, 0L, struct, srWindow$OFFSET, srWindow$LAYOUT.byteSize());
	    }

	    private static final GroupLayout dwMaximumWindowSize$LAYOUT = (GroupLayout)$LAYOUT.select(groupElement("dwMaximumWindowSize"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * COORD dwMaximumWindowSize
	     * }
	     */
	    public static final GroupLayout dwMaximumWindowSize$layout() {
	        return dwMaximumWindowSize$LAYOUT;
	    }

	    private static final long dwMaximumWindowSize$OFFSET = 18;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * COORD dwMaximumWindowSize
	     * }
	     */
	    public static final long dwMaximumWindowSize$offset() {
	        return dwMaximumWindowSize$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * COORD dwMaximumWindowSize
	     * }
	     */
	    public static MemorySegment dwMaximumWindowSize(MemorySegment struct) {
	        return struct.asSlice(dwMaximumWindowSize$OFFSET, dwMaximumWindowSize$LAYOUT.byteSize());
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * COORD dwMaximumWindowSize
	     * }
	     */
	    public static void dwMaximumWindowSize(MemorySegment struct, MemorySegment fieldValue) {
	        MemorySegment.copy(fieldValue, 0L, struct, dwMaximumWindowSize$OFFSET, dwMaximumWindowSize$LAYOUT.byteSize());
	    }

	    /**
	     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
	     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
	     */
	    public static MemorySegment asSlice(MemorySegment array, long index) {
	        return array.asSlice(layout().byteSize() * index);
	    }

	    /**
	     * The size (in bytes) of this struct
	     */
	    public static long sizeof() { return layout().byteSize(); }

	    /**
	     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
	     */
	    public static MemorySegment allocate(SegmentAllocator allocator) {
	        return allocator.allocate(layout());
	    }

	    /**
	     * Allocate an array of size {@code elementCount} using {@code allocator}.
	     * The returned segment has size {@code elementCount * layout().byteSize()}.
	     */
	    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
	        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
	    }

	    /**
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction) (if any).
	     * The returned segment has size {@code layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
	        return reinterpret(addr, 1, arena, cleanup);
	    }

	    /**
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction) (if any).
	     * The returned segment has size {@code elementCount * layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
	        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
	    }
	}

	
	public final static class _SMALL_RECT {

	    _SMALL_RECT() {
	        // Should not be called directly
	    }

	    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
	        JAVA_SHORT.withName("Left"),
	        JAVA_SHORT.withName("Top"),
	        JAVA_SHORT.withName("Right"),
	        JAVA_SHORT.withName("Bottom")
	    ).withName("_SMALL_RECT");

	    /**
	     * The layout of this struct
	     */
	    public static final GroupLayout layout() {
	        return $LAYOUT;
	    }

	    private static final OfShort Left$LAYOUT = (OfShort)$LAYOUT.select(groupElement("Left"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * SHORT Left
	     * }
	     */
	    public static final OfShort Left$layout() {
	        return Left$LAYOUT;
	    }

	    private static final long Left$OFFSET = 0;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * SHORT Left
	     * }
	     */
	    public static final long Left$offset() {
	        return Left$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * SHORT Left
	     * }
	     */
	    public static short Left(MemorySegment struct) {
	        return struct.get(Left$LAYOUT, Left$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * SHORT Left
	     * }
	     */
	    public static void Left(MemorySegment struct, short fieldValue) {
	        struct.set(Left$LAYOUT, Left$OFFSET, fieldValue);
	    }

	    private static final OfShort Top$LAYOUT = (OfShort)$LAYOUT.select(groupElement("Top"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * SHORT Top
	     * }
	     */
	    public static final OfShort Top$layout() {
	        return Top$LAYOUT;
	    }

	    private static final long Top$OFFSET = 2;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * SHORT Top
	     * }
	     */
	    public static final long Top$offset() {
	        return Top$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * SHORT Top
	     * }
	     */
	    public static short Top(MemorySegment struct) {
	        return struct.get(Top$LAYOUT, Top$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * SHORT Top
	     * }
	     */
	    public static void Top(MemorySegment struct, short fieldValue) {
	        struct.set(Top$LAYOUT, Top$OFFSET, fieldValue);
	    }

	    private static final OfShort Right$LAYOUT = (OfShort)$LAYOUT.select(groupElement("Right"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * SHORT Right
	     * }
	     */
	    public static final OfShort Right$layout() {
	        return Right$LAYOUT;
	    }

	    private static final long Right$OFFSET = 4;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * SHORT Right
	     * }
	     */
	    public static final long Right$offset() {
	        return Right$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * SHORT Right
	     * }
	     */
	    public static short Right(MemorySegment struct) {
	        return struct.get(Right$LAYOUT, Right$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * SHORT Right
	     * }
	     */
	    public static void Right(MemorySegment struct, short fieldValue) {
	        struct.set(Right$LAYOUT, Right$OFFSET, fieldValue);
	    }

	    private static final OfShort Bottom$LAYOUT = (OfShort)$LAYOUT.select(groupElement("Bottom"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * SHORT Bottom
	     * }
	     */
	    public static final OfShort Bottom$layout() {
	        return Bottom$LAYOUT;
	    }

	    private static final long Bottom$OFFSET = 6;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * SHORT Bottom
	     * }
	     */
	    public static final long Bottom$offset() {
	        return Bottom$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * SHORT Bottom
	     * }
	     */
	    public static short Bottom(MemorySegment struct) {
	        return struct.get(Bottom$LAYOUT, Bottom$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * SHORT Bottom
	     * }
	     */
	    public static void Bottom(MemorySegment struct, short fieldValue) {
	        struct.set(Bottom$LAYOUT, Bottom$OFFSET, fieldValue);
	    }

	    /**
	     * Obtains a slice of {@code arrayParam} which selects the array element at {@code index}.
	     * The returned segment has address {@code arrayParam.address() + index * layout().byteSize()}
	     */
	    public static MemorySegment asSlice(MemorySegment array, long index) {
	        return array.asSlice(layout().byteSize() * index);
	    }

	    /**
	     * The size (in bytes) of this struct
	     */
	    public static long sizeof() { return layout().byteSize(); }

	    /**
	     * Allocate a segment of size {@code layout().byteSize()} using {@code allocator}
	     */
	    public static MemorySegment allocate(SegmentAllocator allocator) {
	        return allocator.allocate(layout());
	    }

	    /**
	     * Allocate an array of size {@code elementCount} using {@code allocator}.
	     * The returned segment has size {@code elementCount * layout().byteSize()}.
	     */
	    public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
	        return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
	    }

	    /**
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction) (if any).
	     * The returned segment has size {@code layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
	        return reinterpret(addr, 1, arena, cleanup);
	    }

	    /**
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction) (if any).
	     * The returned segment has size {@code elementCount * layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
	        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
	    }
	}

}
