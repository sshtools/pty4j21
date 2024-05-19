package com.pty4j.unix;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfShort;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.pty4j.Platform;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.pty4j.unix.LibC.LibCHelper;
import com.pty4j.util.PtyUtil;

/**
 * @author traff
 */
class NativePtyExecutor implements PtyExecutor {

  NativePtyExecutor(@NotNull String libraryName) {
  }

  @Override
  public int execPty(String full_path, String[] argv, String[] envp, String dirpath, String pts_name, int fdm,
                     String err_pts_name, int err_fdm, boolean console) {
    try (var offHeap = Arena.ofConfined()) {
      var argvMem = offHeap.allocate( LibCHelper.POINTER, argv.length + 1); 
      for(int i = 0 ; i < argv.length; i++) {
        argvMem.setAtIndex(LibCHelper.POINTER, i, offHeap.allocateFrom(argv[i]));
      }
      argvMem.setAtIndex(LibCHelper.POINTER, argv.length, MemorySegment.NULL);

      var envpMem = offHeap.allocate( LibCHelper.POINTER, envp.length+ 1); 
      for(int i = 0 ; i < envp.length; i++) {
        envpMem.setAtIndex(LibCHelper.POINTER, i, offHeap.allocateFrom(envp[i]));
      }
      envpMem.setAtIndex(LibCHelper.POINTER, envp.length, MemorySegment.NULL);
      
      return LibPty.exec_pty(offHeap.allocateFrom(full_path), 
                             argvMem, envpMem, 
                             offHeap.allocateFrom(dirpath), 
                             offHeap.allocateFrom(pts_name), 
                             fdm, 
                             offHeap.allocateFrom(err_pts_name == null ? "" : err_pts_name)
                             , err_fdm, console ? 1 : 0);
    }
  }

  @Override
  public int waitForProcessExitAndGetExitCode(int pid) {
    return LibPty.wait_for_child_process_exit(pid);
  }

  @Override
  public @NotNull WinSize getWindowSize(int fd, @Nullable PtyProcess process) throws UnixPtyException {
      try (var offHeap = Arena.ofConfined()) {
    	  var ws = winsize.allocate(offHeap);
        var errno = LibPty.get_window_size(fd, ws);
        if (errno != 0) {
          throw new UnixPtyException("Failed to get window size:" +
            " fd=" + fd + (LibPty.is_valid_fd(fd) > 0 ? "(valid)" : "(invalid)") +
            ", " + getErrorInfo(errno, process), errno);
        }
        return new WinSize(winsize.ws_col(ws), winsize.ws_row(ws));
      }
  }

  @Override
  public void setWindowSize(int fd, @NotNull WinSize winSize, @Nullable PtyProcess process) throws UnixPtyException {
    try (var offHeap = Arena.ofConfined()) {
      var ws = winsize.allocate(offHeap);
      winsize.ws_col(ws, (short)winSize.getColumns());
      winsize.ws_row(ws, (short)winSize.getRows());
      var errno = LibPty.set_window_size(fd, ws);
      if (errno != 0) {
        boolean validFd = LibPty.is_valid_fd(fd) > 0;
        String message = "Failed to set window size: [" + winSize + "]" + ", fd=" + fd
            + (validFd ? "(valid)" : "(invalid)") + ", " + getErrorInfo(errno, process);
        throw new UnixPtyException(message, errno);
      }
    }
  }

  private static @NotNull String getErrorInfo(int errno, @Nullable PtyProcess process) {
    String message = "errno=" + errno + "(" + (errno != -1 ? PtyHelpers.getInstance().strerror(errno) : "unknown") + ")";
    if (process != null) {
      Integer exitCode = getExitCode(process);
      message += ", pid:" + process.pid() + ", running:" + process.isAlive() +
        ", exit code:" + (exitCode != null ? exitCode : "N/A");
    }
    return message;
  }

  private static @Nullable Integer getExitCode(@NotNull PtyProcess process) {
    Integer exitCode = null;
    try {
      exitCode = process.exitValue();
    }
    catch (IllegalThreadStateException ignored) {
    }
    return exitCode;
  }
  
  private final static class LibPty {
    private static int exec_pty(MemorySegment path, MemorySegment argv, MemorySegment envp, MemorySegment dirpath,
                               MemorySegment pts_name, int fdm, MemorySegment err_pts_name, int err_fdm, int console) {
      try {
        return (int)LibPtyHelper.downcallHandle("exec_pty",
                    FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, LibCHelper.POINTER,
                    		LibCHelper.POINTER, LibCHelper.POINTER, LibCHelper.POINTER, JAVA_INT,
                    		LibCHelper.POINTER, JAVA_INT, JAVA_INT))
						.invokeExact(path, argv, envp, dirpath, pts_name, fdm, err_pts_name, err_fdm, console);
      } catch (Throwable ex$) {
        throw new AssertionError("should not reach here", ex$);
      }
    }
    
    private static int wait_for_child_process_exit(int child_pid) {
      try {
        return (int) LibPtyHelper
               .downcallHandle("wait_for_child_process_exit", FunctionDescriptor.of(JAVA_INT, JAVA_INT))
               .invokeExact(child_pid);
      } catch (Throwable ex$) {
        throw new AssertionError("should not reach here", ex$);
      }
    }
    
    private static int get_window_size(int fd, MemorySegment size) {
      try {
        return (int) LibPtyHelper
               .downcallHandle("get_window_size", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER))
               .invokeExact(fd, size);
      } catch (Throwable ex$) {
          throw new AssertionError("should not reach here", ex$);
      }
    }
    
    private static int set_window_size(int fd, MemorySegment size) {
      try {
        return (int) LibPtyHelper
               .downcallHandle("set_window_size", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER))
               .invokeExact(fd, size);
      } catch (Throwable ex$) {
        throw new AssertionError("should not reach here", ex$);
      }
    }
	
	private static int is_valid_fd(int fd) {
      try {
        return (int)LibPtyHelper.downcallHandle("is_valid_fd",FunctionDescriptor.of(JAVA_INT,JAVA_INT)).invokeExact(fd);
      } catch (Throwable ex$) {
        throw new AssertionError("should not reach here", ex$);
      }
    }
  }
  
  final class LibPtyHelper {

    private static final Linker LINKER = Linker.nativeLinker();
    private static final MethodHandles.Lookup MH_LOOKUP = MethodHandles.lookup();
    private static final SymbolLookup SYMBOL_LOOKUP;
    private static final SegmentAllocator THROWING_ALLOCATOR = (x, y) -> {
      throw new AssertionError("should not reach here");
    };

    final static SegmentAllocator CONSTANT_ALLOCATOR = (size, align) -> Arena.ofAuto().allocate(size, align);

    static {
      SymbolLookup loaderLookup = SymbolLookup.libraryLookup(PtyUtil.resolveNativeFile(Platform.isMac() ? "libpty.dylib" : "libpty.so").toPath(), Arena.global());
      SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LINKER.defaultLookup().find(name));
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private LibPtyHelper() {
    }

    static <T> T requireNonNull(T obj, String symbolName) {
      if (obj == null) {
        throw new UnsatisfiedLinkError("unresolved symbol: " + symbolName);
      }
      return obj;
    }

    static MemorySegment lookupGlobalVariable(String name, MemoryLayout layout) {
      return SYMBOL_LOOKUP.find(name).map(s -> s.reinterpret(layout.byteSize())).orElse(null);
    }

    static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
      return SYMBOL_LOOKUP.find(name).map(addr -> LINKER.downcallHandle(addr, fdesc)).orElse(null);
    }

    static MethodHandle downcallHandle(FunctionDescriptor fdesc) {
      return LINKER.downcallHandle(fdesc);
    }

    static MethodHandle downcallHandleVariadic(String name, FunctionDescriptor fdesc) {
      return SYMBOL_LOOKUP.find(name).map(addr -> VarargsInvoker.make(addr, fdesc)).orElse(null);
    }

    static MethodHandle upcallHandle(Class<?> fi, String name, FunctionDescriptor fdesc) {
      try {
        return MH_LOOKUP.findVirtual(fi, name, fdesc.toMethodType());
      } catch (Throwable ex) {
        throw new AssertionError(ex);
      }
    }

    static <Z> MemorySegment upcallStub(MethodHandle fiHandle, Z z, FunctionDescriptor fdesc, Arena scope) {
      try {
        fiHandle = fiHandle.bindTo(z);
        return LINKER.upcallStub(fiHandle, fdesc, scope);
      } catch (Throwable ex) {
        throw new AssertionError(ex);
      }
    }

    static MemorySegment asArray(MemorySegment addr, MemoryLayout layout, int numElements, Arena arena) {
      return addr.reinterpret(numElements * layout.byteSize(), arena, null);
    }

    // Internals only below this point

    private static final class VarargsInvoker {
      private static final MethodHandle INVOKE_MH = makeHandle(); /* Weird compiler error */

      static MethodHandle makeHandle() {
        try {
          return MethodHandles.lookup().findVirtual(VarargsInvoker.class, "invoke",
              MethodType.methodType(Object.class, SegmentAllocator.class, Object[].class));
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
        }
      }

      static MethodHandle make(MemorySegment symbol, FunctionDescriptor function) {
        VarargsInvoker invoker = new VarargsInvoker();
        MethodHandle handle = INVOKE_MH.bindTo(invoker).asCollector(Object[].class,
            function.argumentLayouts().size() + 1);
        MethodType mtype = MethodType.methodType(
            function.returnLayout().isPresent() ? carrier(function.returnLayout().get(), true) : void.class);
        for (MemoryLayout layout : function.argumentLayouts()) {
          mtype = mtype.appendParameterTypes(carrier(layout, false));
        }
        mtype = mtype.appendParameterTypes(Object[].class);
        boolean needsAllocator = function.returnLayout().isPresent()
            && function.returnLayout().get() instanceof GroupLayout;
        if (needsAllocator) {
          mtype = mtype.insertParameterTypes(0, SegmentAllocator.class);
        } else {
          handle = MethodHandles.insertArguments(handle, 0, THROWING_ALLOCATOR);
        }
        return handle.asType(mtype);
      }

      static Class<?> carrier(MemoryLayout layout, boolean ret) {
        if (layout instanceof ValueLayout valueLayout) {
          return valueLayout.carrier();
        } else if (layout instanceof GroupLayout) {
          return MemorySegment.class;
        } else {
          throw new AssertionError("Cannot get here!");
        }
      }
    }
  }

  public class winsize {

	    winsize() {
	        // Should not be called directly
	    }

	    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
	        ValueLayout.JAVA_SHORT.withName("ws_row"),
	        ValueLayout.JAVA_SHORT.withName("ws_col"),
	        ValueLayout.JAVA_SHORT.withName("ws_xpixel"),
	        ValueLayout.JAVA_SHORT.withName("ws_ypixel")
	    ).withName("winsize");

	    /**
	     * The layout of this struct
	     */
	    public static final GroupLayout layout() {
	        return $LAYOUT;
	    }

	    private static final OfShort ws_row$LAYOUT = (OfShort)$LAYOUT.select(groupElement("ws_row"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * unsigned short ws_row
	     * }
	     */
	    public static final OfShort ws_row$layout() {
	        return ws_row$LAYOUT;
	    }

	    private static final long ws_row$OFFSET = 0;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * unsigned short ws_row
	     * }
	     */
	    public static final long ws_row$offset() {
	        return ws_row$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_row
	     * }
	     */
	    public static short ws_row(MemorySegment struct) {
	        return struct.get(ws_row$LAYOUT, ws_row$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_row
	     * }
	     */
	    public static void ws_row(MemorySegment struct, short fieldValue) {
	        struct.set(ws_row$LAYOUT, ws_row$OFFSET, fieldValue);
	    }

	    private static final OfShort ws_col$LAYOUT = (OfShort)$LAYOUT.select(groupElement("ws_col"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * unsigned short ws_col
	     * }
	     */
	    public static final OfShort ws_col$layout() {
	        return ws_col$LAYOUT;
	    }

	    private static final long ws_col$OFFSET = 2;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * unsigned short ws_col
	     * }
	     */
	    public static final long ws_col$offset() {
	        return ws_col$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_col
	     * }
	     */
	    public static short ws_col(MemorySegment struct) {
	        return struct.get(ws_col$LAYOUT, ws_col$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_col
	     * }
	     */
	    public static void ws_col(MemorySegment struct, short fieldValue) {
	        struct.set(ws_col$LAYOUT, ws_col$OFFSET, fieldValue);
	    }

	    private static final OfShort ws_xpixel$LAYOUT = (OfShort)$LAYOUT.select(groupElement("ws_xpixel"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * unsigned short ws_xpixel
	     * }
	     */
	    public static final OfShort ws_xpixel$layout() {
	        return ws_xpixel$LAYOUT;
	    }

	    private static final long ws_xpixel$OFFSET = 4;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * unsigned short ws_xpixel
	     * }
	     */
	    public static final long ws_xpixel$offset() {
	        return ws_xpixel$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_xpixel
	     * }
	     */
	    public static short ws_xpixel(MemorySegment struct) {
	        return struct.get(ws_xpixel$LAYOUT, ws_xpixel$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_xpixel
	     * }
	     */
	    public static void ws_xpixel(MemorySegment struct, short fieldValue) {
	        struct.set(ws_xpixel$LAYOUT, ws_xpixel$OFFSET, fieldValue);
	    }

	    private static final OfShort ws_ypixel$LAYOUT = (OfShort)$LAYOUT.select(groupElement("ws_ypixel"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * unsigned short ws_ypixel
	     * }
	     */
	    public static final OfShort ws_ypixel$layout() {
	        return ws_ypixel$LAYOUT;
	    }

	    private static final long ws_ypixel$OFFSET = 6;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * unsigned short ws_ypixel
	     * }
	     */
	    public static final long ws_ypixel$offset() {
	        return ws_ypixel$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_ypixel
	     * }
	     */
	    public static short ws_ypixel(MemorySegment struct) {
	        return struct.get(ws_ypixel$LAYOUT, ws_ypixel$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * unsigned short ws_ypixel
	     * }
	     */
	    public static void ws_ypixel(MemorySegment struct, short fieldValue) {
	        struct.set(ws_ypixel$LAYOUT, ws_ypixel$OFFSET, fieldValue);
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
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
	     * The returned segment has size {@code layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
	        return reinterpret(addr, 1, arena, cleanup);
	    }

	    /**
	     * Reinterprets {@code addr} using target {@code arena} and {@code cleanupAction} (if any).
	     * The returned segment has size {@code elementCount * layout().byteSize()}
	     */
	    public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena, Consumer<MemorySegment> cleanup) {
	        return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
	    }
	}



}
