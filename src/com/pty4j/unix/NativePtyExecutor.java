package com.pty4j.unix;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

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
      /* TODO not a scooby doo if this works or not */
      var argvMem = offHeap.allocateArray( LibCHelper.POINTER, argv.length + 1); 
      for(int i = 0 ; i < argv.length; i++) {
        argvMem.setAtIndex(LibCHelper.POINTER, i, offHeap.allocateUtf8String(argv[i]));
      }
      argvMem.setAtIndex(LibCHelper.POINTER, argv.length, MemorySegment.NULL);

      var envpMem = offHeap.allocateArray( LibCHelper.POINTER, envp.length+ 1); 
      for(int i = 0 ; i < envp.length; i++) {
        envpMem.setAtIndex(LibCHelper.POINTER, i, offHeap.allocateUtf8String(envp[i]));
      }
      envpMem.setAtIndex(LibCHelper.POINTER, envp.length, MemorySegment.NULL);
      
      return LibPty.exec_pty(offHeap.allocateUtf8String(full_path), 
                             argvMem, envpMem, 
                             offHeap.allocateUtf8String(dirpath), 
                             offHeap.allocateUtf8String(pts_name), 
                             fdm, 
                             offHeap.allocateUtf8String(err_pts_name == null ? "" : err_pts_name)
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
        return new WinSize(winsize.ws_col$get(ws), winsize.ws_row$get(ws));
      }
  }

  @Override
  public void setWindowSize(int fd, @NotNull WinSize winSize, @Nullable PtyProcess process) throws UnixPtyException {
    try (var offHeap = Arena.ofConfined()) {
      var ws = winsize.allocate(offHeap);
      winsize.ws_col$set(ws, winSize.ws_col);
      winsize.ws_row$set(ws, winSize.ws_row);
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
    static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(JAVA_BYTE));

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
      private static final MethodHandle INVOKE_MH;

      static {
        try {
          INVOKE_MH = MethodHandles.lookup().findVirtual(VarargsInvoker.class, "invoke",
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

    final class constants$0 {

      // Suppresses default constructor, ensuring non-instantiability.
      private constants$0() {
      }

      static final StructLayout const$0 = MemoryLayout.structLayout(JAVA_SHORT.withName("ws_row"),
          JAVA_SHORT.withName("ws_col"), JAVA_SHORT.withName("ws_xpixel"), JAVA_SHORT.withName("ws_ypixel"))
          .withName("winsize");
      static final VarHandle const$1 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("ws_row"));
      static final VarHandle const$2 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("ws_col"));
      static final VarHandle const$3 = constants$0.const$0
          .varHandle(MemoryLayout.PathElement.groupElement("ws_xpixel"));
      static final VarHandle const$4 = constants$0.const$0
          .varHandle(MemoryLayout.PathElement.groupElement("ws_ypixel"));
      static final StructLayout const$5 = MemoryLayout
          .structLayout(JAVA_SHORT.withName("c_iflag"), JAVA_SHORT.withName("c_oflag"), JAVA_SHORT.withName("c_cflag"),
              JAVA_SHORT.withName("c_lflag"), JAVA_BYTE.withName("c_line"),
              MemoryLayout.sequenceLayout(8, JAVA_BYTE).withName("c_cc"), MemoryLayout.paddingLayout(1))
          .withName("termio");
    }

    public static MemoryLayout $LAYOUT() {
      return constants$0.const$0;
    }

    public static VarHandle ws_row$VH() {
      return constants$0.const$1;
    }

    /**
     * Getter for field:
     * {@snippet : * unsigned short ws_row;
     * }
     */
    public static short ws_row$get(MemorySegment seg) {
      return (short) constants$0.const$1.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet : * unsigned short ws_row;
     * }
     */
    public static void ws_row$set(MemorySegment seg, short x) {
      constants$0.const$1.set(seg, x);
    }

    public static short ws_row$get(MemorySegment seg, long index) {
      return (short) constants$0.const$1.get(seg.asSlice(index * sizeof()));
    }

    public static void ws_row$set(MemorySegment seg, long index, short x) {
      constants$0.const$1.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle ws_col$VH() {
      return constants$0.const$2;
    }

    /**
     * Getter for field:
     * {@snippet : * unsigned short ws_col;
     * }
     */
    public static short ws_col$get(MemorySegment seg) {
      return (short) constants$0.const$2.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet : * unsigned short ws_col;
     * }
     */
    public static void ws_col$set(MemorySegment seg, short x) {
      constants$0.const$2.set(seg, x);
    }

    public static short ws_col$get(MemorySegment seg, long index) {
      return (short) constants$0.const$2.get(seg.asSlice(index * sizeof()));
    }

    public static void ws_col$set(MemorySegment seg, long index, short x) {
      constants$0.const$2.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle ws_xpixel$VH() {
      return constants$0.const$3;
    }

    /**
     * Getter for field:
     * {@snippet : * unsigned short ws_xpixel;
     * }
     */
    public static short ws_xpixel$get(MemorySegment seg) {
      return (short) constants$0.const$3.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet : * unsigned short ws_xpixel;
     * }
     */
    public static void ws_xpixel$set(MemorySegment seg, short x) {
      constants$0.const$3.set(seg, x);
    }

    public static short ws_xpixel$get(MemorySegment seg, long index) {
      return (short) constants$0.const$3.get(seg.asSlice(index * sizeof()));
    }

    public static void ws_xpixel$set(MemorySegment seg, long index, short x) {
      constants$0.const$3.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle ws_ypixel$VH() {
      return constants$0.const$4;
    }

    /**
     * Getter for field:
     * {@snippet : * unsigned short ws_ypixel;
     * }
     */
    public static short ws_ypixel$get(MemorySegment seg) {
      return (short) constants$0.const$4.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet : * unsigned short ws_ypixel;
     * }
     */
    public static void ws_ypixel$set(MemorySegment seg, short x) {
      constants$0.const$4.set(seg, x);
    }

    public static short ws_ypixel$get(MemorySegment seg, long index) {
      return (short) constants$0.const$4.get(seg.asSlice(index * sizeof()));
    }

    public static void ws_ypixel$set(MemorySegment seg, long index, short x) {
      constants$0.const$4.set(seg.asSlice(index * sizeof()), x);
    }

    public static long sizeof() {
      return $LAYOUT().byteSize();
    }

    public static MemorySegment allocate(SegmentAllocator allocator) {
      return allocator.allocate($LAYOUT());
    }

    public static MemorySegment allocateArray(long len, SegmentAllocator allocator) {
      return allocator.allocate(MemoryLayout.sequenceLayout(len, $LAYOUT()));
    }

    public static MemorySegment ofAddress(MemorySegment addr, Arena arena) {
      return LibPtyHelper.asArray(addr, $LAYOUT(), 1, arena);
    }
  }

}
