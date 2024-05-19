package com.pty4j.unix;

import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.MemoryLayout.PathElement.sequenceElement;
import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfShort;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.util.function.Consumer;

import com.pty4j.Native;

public class LibC {
  
  public static final Linker LINKER = Linker.nativeLinker();
  private static final SegmentAllocator THROWING_ALLOCATOR = (x, y) -> {
    throw new AssertionError("should not reach here");
  };

  public static int O_RDWR = 2;
  public static int O_NOCTTY = 256;

  public static int EINTR = 4;
  public static int EAGAIN = 11;

  public static int TCSANOW = 0;

  public static int ICRNL = 256;
  public static int IXON = 1024;
  public static int IXANY = 2048;
  public static int ISIG = 1;
  public static int ICANON = 2;
  public static int IEXTEN = 32768;

  public static int ECHO = 8;
  public static int ECHOE = 16;

  public static int B38400 = 15;

  public static int OPOST = 1;
  public static int ONLCR = 4;

  public static int CREAD = 128;
  public static int CS8 = 48;

  public static int BRKINT = 2;

  public static int POLLIN = 1;

  public static int VINTR = 0;
  public static int VQUIT = 1;
  public static int VERASE = 2;
  public static int VKILL = 3;
  public static int VEOF = 4;
  public static int VSTART = 8;
  public static int VSTOP = 9;
  public static int VSUSP = 10;
  public static int VREPRINT = 12;
  public static int VWERASE = 14;
  
  private final static class getpid {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("getpid", DESC);
  }

  public static int getpid() {
    try {
      return (int) getpid.HANDLE.invokeExact();
    } catch (RuntimeException re) {
      throw re;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }
  
  private final static class getppid {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("getppid", DESC);
  }

  public static int getppid() {
    try {
      return (int) getppid.HANDLE.invokeExact();
    } catch (RuntimeException re) {
      throw re;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }
  
  private final static class chdir {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("chdir", DESC);
  }

  public static int chdir(MemorySegment __path) {
    try {
      return (int) chdir.HANDLE.invokeExact(__path);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class setpgid {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("setpgid", DESC);
  }

  public static int setpgid(int __pid, int __pgid) {
    try {
      return (int) setpgid.HANDLE.invokeExact(__pid, __pgid);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class fork {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("fork", DESC);
  }

  public static int fork() {
    try {
      return (int) fork.HANDLE.invokeExact();
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class close {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("close", DESC);
  }

  public static int close(int __fd) {
    try {
      return (int) close.HANDLE.invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class setsid {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("setsid", DESC);
  }

  public static int setsid() {
    try {
      return (int) setsid.HANDLE.invokeExact();
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class dup2 {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("dup2", DESC);
  }

  public static int dup2(int __fd, int __fd2) {
    try {
      return (int) dup2.HANDLE.invokeExact(__fd, __fd2);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class waitpid {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("waitpid", DESC);
  }

  public static int waitpid(int __pid, MemorySegment __stat_loc, int __options) {
    try {
      return (int) waitpid.HANDLE.invokeExact(__pid, __stat_loc, __options);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class killpg {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("killpg", DESC);
  }

  public static int killpg(int __pgrp, int __sig) {
    try {
      return (int) killpg.HANDLE.invokeExact(__pgrp, __sig);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class kill {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("kill", DESC);
  }

  public static int kill(int __pid, int __sig) {
    try {
      return (int) kill.HANDLE.invokeExact(__pid, __sig);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class read {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("read", DESC);
  }

  public static int read(int __fd, MemorySegment __buf, int __len) {
    try {
      return (int) read.HANDLE.invokeExact(__fd, __buf, __len);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class write {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("write", DESC);
  }

  public static int write(int __fd, MemorySegment __buf, int __len) {
    try {
      return (int) write.HANDLE.invokeExact(__fd, __buf, __len);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class sigprocmask {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("sigprocmask",DESC);
  }

  public static int sigprocmask(int __how, MemorySegment __set, MemorySegment __oset) {
    try {
      return (int) sigprocmask.HANDLE.invokeExact(__how, __set, __oset);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class grantpt {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("grantpt", DESC);
  }

  public static int grantpt(int __fdm) {
    try {
      return (int) grantpt.HANDLE.invokeExact(__fdm);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class unlockpt {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("unlockpt", DESC);
  }

  public static int unlockpt(int __fdm) {
    try {
      return (int) unlockpt.HANDLE.invokeExact(__fdm);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class ptsname {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("ptsname", DESC);
  }

  public static MemorySegment ptsname(int __fd) {
    try {
      return (java.lang.foreign.MemorySegment) ptsname.HANDLE.invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class unsetenv {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("unsetenv", DESC);
  }

  public static int unsetenv(MemorySegment __name) {
    try {
      return (int) unsetenv.HANDLE.invokeExact(__name);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class select {
	  static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, LibCHelper.POINTER,
              LibCHelper.POINTER, LibCHelper.POINTER);
  }

  public static int select(int __nfds, MemorySegment __readfds, MemorySegment __writefds, MemorySegment __exceptfds,
      MemorySegment __timeout) {
    try {
      return (int) LibCHelper
          .downcallHandle("select", select.DESC)
          .invokeExact(__nfds, __readfds, __writefds, __exceptfds, __timeout);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class strerror {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("strerror", DESC);
  }

  public static MemorySegment strerror(int __errno) {
    try {
      return (java.lang.foreign.MemorySegment) strerror.HANDLE.invokeExact(__errno);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class pipe {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("pipe", DESC);
  }

  public static int pipe(MemorySegment __pipedes) {
    try {
      return (int) pipe.HANDLE.invokeExact(__pipedes);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class open {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandleVariadic("open", DESC);
  }

  public static int open(MemorySegment __file, int __oflag, Object... x2) {
    try {
      return (int) open.HANDLE.invokeExact(__file, __oflag, x2);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class tcdrain {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("tcdrain", DESC);
  }

  public static int tcdrain(int __fd) {
    try {
      return (int) tcdrain.HANDLE.invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class poll {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, JAVA_LONG, JAVA_INT);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("poll", DESC);
  }

  public static int poll(MemorySegment __fds, long __nfds, int __timeout) {
    try {
      return (int) poll.HANDLE.invokeExact(__fds, __nfds, __timeout);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class tcsetattr {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("tcsetattr", DESC);
  }

  public static int tcsetattr(int __fd, int __optional_actions, MemorySegment __termios_p) {
    try {
      return (int) tcsetattr.HANDLE.invokeExact(__fd, __optional_actions, __termios_p);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }
  
  private final static class tcgetattr {
	  final static FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER);
	  final static MethodHandle HANDLE = LibCHelper.downcallHandle("tcgetattr", DESC);
  }

  public static int tcgetattr(int __fd, MemorySegment __termios_p) {
    try {
      return (int) tcgetattr.HANDLE.invokeExact(__fd, __termios_p);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public final static class LibCHelper {

    private static final SymbolLookup SYMBOL_LOOKUP;
    public static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Long.MAX_VALUE, JAVA_BYTE));
    

    // Setup handles
    static Linker.Option ccs = Linker.Option.captureCallState("errno");
    static StructLayout capturedStateLayout = Linker.Option.captureStateLayout();
    static VarHandle errnoHandle = capturedStateLayout.varHandle(PathElement.groupElement("errno"));

    static {
      //SymbolLookup loaderLookup = Native.tryLibraries("libc", "c", "libc.so.6");
    	SymbolLookup loaderLookup = Native.load("c", Arena.global());
      SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LINKER.defaultLookup().find(name));
    }

    // Suppresses default constructor, ensuring non-instantiability.
    private LibCHelper() {
    }

    public static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
      return SYMBOL_LOOKUP.find(name).map(addr -> LINKER.downcallHandle(addr, fdesc)).orElse(null);
    }

    static MethodHandle downcallHandleVariadic(String name, FunctionDescriptor fdesc) {
      return SYMBOL_LOOKUP.find(name).map(addr -> VarargsInvoker.make(addr, fdesc)).orElse(null);
    }

    static MethodHandle downcallHandle(FunctionDescriptor fdesc) {
      return LINKER.downcallHandle(fdesc);
    }

    static MemorySegment asArray(MemorySegment addr, MemoryLayout layout, int numElements, Arena arena) {
      return addr.reinterpret(numElements * layout.byteSize(), arena, null);
    }

    private static final class VarargsInvoker {
      private static final MethodHandle INVOKE_MH;
      private final MemorySegment symbol;
      private final FunctionDescriptor function;

      private VarargsInvoker(MemorySegment symbol, FunctionDescriptor function) {
        this.symbol = symbol;
        this.function = function;
      }

      static {
        try {
          INVOKE_MH = MethodHandles.lookup().findVirtual(VarargsInvoker.class, "invoke",
              MethodType.methodType(Object.class, SegmentAllocator.class, Object[].class));
        } catch (ReflectiveOperationException e) {
          throw new RuntimeException(e);
        }
      }

      static MethodHandle make(MemorySegment symbol, FunctionDescriptor function) {
        VarargsInvoker invoker = new VarargsInvoker(symbol, function);
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

      @SuppressWarnings("unused")
      private Object invoke(SegmentAllocator allocator, Object[] args) throws Throwable {
        // one trailing Object[]
        int nNamedArgs = function.argumentLayouts().size();
        assert (args.length == nNamedArgs + 1);
        // The last argument is the array of vararg collector
        Object[] unnamedArgs = (Object[]) args[args.length - 1];

        int argsCount = nNamedArgs + unnamedArgs.length;
        Class<?>[] argTypes = new Class<?>[argsCount];
        MemoryLayout[] argLayouts = new MemoryLayout[nNamedArgs + unnamedArgs.length];

        int pos = 0;
        for (pos = 0; pos < nNamedArgs; pos++) {
          argLayouts[pos] = function.argumentLayouts().get(pos);
        }

        assert pos == nNamedArgs;
        for (Object o : unnamedArgs) {
          argLayouts[pos] = variadicLayout(normalize(o.getClass()));
          pos++;
        }
        assert pos == argsCount;

        FunctionDescriptor f = (function.returnLayout().isEmpty()) ? FunctionDescriptor.ofVoid(argLayouts)
            : FunctionDescriptor.of(function.returnLayout().get(), argLayouts);
        MethodHandle mh = LINKER.downcallHandle(symbol, f);
        boolean needsAllocator = function.returnLayout().isPresent()
            && function.returnLayout().get() instanceof GroupLayout;
        if (needsAllocator) {
          mh = mh.bindTo(allocator);
        }
        // flatten argument list so that it can be passed to an asSpreader MH
        Object[] allArgs = new Object[nNamedArgs + unnamedArgs.length];
        System.arraycopy(args, 0, allArgs, 0, nNamedArgs);
        System.arraycopy(unnamedArgs, 0, allArgs, nNamedArgs, unnamedArgs.length);

        return mh.asSpreader(Object[].class, argsCount).invoke(allArgs);
      }

      private static Class<?> unboxIfNeeded(Class<?> clazz) {
        if (clazz == Boolean.class) {
          return boolean.class;
        } else if (clazz == Void.class) {
          return void.class;
        } else if (clazz == Byte.class) {
          return byte.class;
        } else if (clazz == Character.class) {
          return char.class;
        } else if (clazz == Short.class) {
          return short.class;
        } else if (clazz == Integer.class) {
          return int.class;
        } else if (clazz == Long.class) {
          return long.class;
        } else if (clazz == Float.class) {
          return float.class;
        } else if (clazz == Double.class) {
          return double.class;
        } else {
          return clazz;
        }
      }

      private Class<?> promote(Class<?> c) {
        if (c == byte.class || c == char.class || c == short.class || c == int.class) {
          return long.class;
        } else if (c == float.class) {
          return double.class;
        } else {
          return c;
        }
      }

      private Class<?> normalize(Class<?> c) {
        c = unboxIfNeeded(c);
        if (c.isPrimitive()) {
          return promote(c);
        }
        if (c == MemorySegment.class) {
          return MemorySegment.class;
        }
        throw new IllegalArgumentException("Invalid type for ABI: " + c.getTypeName());
      }

      private MemoryLayout variadicLayout(Class<?> c) {
        if (c == long.class) {
          return JAVA_LONG;
        } else if (c == double.class) {
          return JAVA_DOUBLE;
        } else if (c == MemorySegment.class) {
          return ADDRESS;
        } else {
          throw new IllegalArgumentException("Unhandled variadic argument class: " + c);
        }
      }
    }
  }

  public class fd_set {

	    fd_set() {
	        // Should not be called directly
	    }

	    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
	        MemoryLayout.sequenceLayout(16, ValueLayout.JAVA_LONG).withName("__fds_bits")
	    ).withName("$anon$59:9");

	    /**
	     * The layout of this struct
	     */
	    public static final GroupLayout layout() {
	        return $LAYOUT;
	    }

	    private static final SequenceLayout __fds_bits$LAYOUT = (SequenceLayout)$LAYOUT.select(groupElement("__fds_bits"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static final SequenceLayout __fds_bits$layout() {
	        return __fds_bits$LAYOUT;
	    }

	    private static final long __fds_bits$OFFSET = 0;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static final long __fds_bits$offset() {
	        return __fds_bits$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static MemorySegment __fds_bits(MemorySegment struct) {
	        return struct.asSlice(__fds_bits$OFFSET, __fds_bits$LAYOUT.byteSize());
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static void __fds_bits(MemorySegment struct, MemorySegment fieldValue) {
	        MemorySegment.copy(fieldValue, 0L, struct, __fds_bits$OFFSET, __fds_bits$LAYOUT.byteSize());
	    }

	    private static long[] __fds_bits$DIMS = { 16 };

	    /**
	     * Dimensions for array field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static long[] __fds_bits$dimensions() {
	        return __fds_bits$DIMS;
	    }
	    private static final VarHandle __fds_bits$ELEM_HANDLE = __fds_bits$LAYOUT.varHandle(sequenceElement());

	    /**
	     * Indexed getter for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static long __fds_bits(MemorySegment struct, long index0) {
	        return (long)__fds_bits$ELEM_HANDLE.get(struct, 0L, index0);
	    }

	    /**
	     * Indexed setter for field:
	     * {@snippet lang=c :
	     * __fd_mask __fds_bits[16]
	     * }
	     */
	    public static void __fds_bits(MemorySegment struct, long index0, long fieldValue) {
	        __fds_bits$ELEM_HANDLE.set(struct, 0L, index0, fieldValue);
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



  public final static class termios {

    final static class constants$0 {

      // Suppresses default constructor, ensuring non-instantiability.
      private constants$0() {
      }

      static final StructLayout const$0 = MemoryLayout
          .structLayout(MemoryLayout.sequenceLayout(2, JAVA_INT).withName("__val")).withName("");
      static final StructLayout const$1 = MemoryLayout.structLayout(JAVA_INT.withName("c_iflag"),
          JAVA_INT.withName("c_oflag"), JAVA_INT.withName("c_cflag"), JAVA_INT.withName("c_lflag"),
          JAVA_BYTE.withName("c_line"), MemoryLayout.sequenceLayout(32, JAVA_BYTE).withName("c_cc"),
          MemoryLayout.paddingLayout(3), JAVA_INT.withName("c_ispeed"), JAVA_INT.withName("c_ospeed"))
          .withName("termios");
      static final VarHandle const$2 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_iflag"));
      static final VarHandle const$3 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_oflag"));
      static final VarHandle const$4 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_cflag"));
      static final VarHandle const$5 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_lflag"));
    }

    final class constants$1 {

      // Suppresses default constructor, ensuring non-instantiability.
      private constants$1() {
      }

      static final VarHandle const$0 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_line"));
      static final VarHandle const$1 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_ispeed"));
      static final VarHandle const$2 = constants$0.const$1.varHandle(MemoryLayout.PathElement.groupElement("c_ospeed"));
      static final FunctionDescriptor const$3 = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER);
      static final MethodHandle const$4 = LibCHelper.downcallHandle("cfgetospeed", constants$1.const$3);
      static final MethodHandle const$5 = LibCHelper.downcallHandle("cfgetispeed", constants$1.const$3);
    }

    public static MemoryLayout $LAYOUT() {
      return constants$0.const$1;
    }

    public static VarHandle c_iflag$VH() {
      return constants$0.const$2;
    }

    /**
     * Getter for field:
     * {@snippet :
     * tcflag_t c_iflag;
     * }
     */
    public static int c_iflag$get(MemorySegment seg) {
      return (int) constants$0.const$2.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * tcflag_t c_iflag;
     * }
     */
    public static void c_iflag$set(MemorySegment seg, int x) {
      constants$0.const$2.set(seg, x);
    }

    public static int c_iflag$get(MemorySegment seg, long index) {
      return (int) constants$0.const$2.get(seg.asSlice(index * sizeof()));
    }

    public static void c_iflag$set(MemorySegment seg, long index, int x) {
      constants$0.const$2.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle c_oflag$VH() {
      return constants$0.const$3;
    }

    /**
     * Getter for field:
     * {@snippet :
     * tcflag_t c_oflag;
     * }
     */
    public static int c_oflag$get(MemorySegment seg) {
      return (int) constants$0.const$3.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * tcflag_t c_oflag;
     * }
     */
    public static void c_oflag$set(MemorySegment seg, int x) {
      constants$0.const$3.set(seg, x);
    }

    public static int c_oflag$get(MemorySegment seg, long index) {
      return (int) constants$0.const$3.get(seg.asSlice(index * sizeof()));
    }

    public static void c_oflag$set(MemorySegment seg, long index, int x) {
      constants$0.const$3.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle c_cflag$VH() {
      return constants$0.const$4;
    }

    /**
     * Getter for field:
     * {@snippet :
     * tcflag_t c_cflag;
     * }
     */
    public static int c_cflag$get(MemorySegment seg) {
      return (int) constants$0.const$4.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * tcflag_t c_cflag;
     * }
     */
    public static void c_cflag$set(MemorySegment seg, int x) {
      constants$0.const$4.set(seg, x);
    }

    public static int c_cflag$get(MemorySegment seg, long index) {
      return (int) constants$0.const$4.get(seg.asSlice(index * sizeof()));
    }

    public static void c_cflag$set(MemorySegment seg, long index, int x) {
      constants$0.const$4.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle c_lflag$VH() {
      return constants$0.const$5;
    }

    /**
     * Getter for field:
     * {@snippet :
     * tcflag_t c_lflag;
     * }
     */
    public static int c_lflag$get(MemorySegment seg) {
      return (int) constants$0.const$5.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * tcflag_t c_lflag;
     * }
     */
    public static void c_lflag$set(MemorySegment seg, int x) {
      constants$0.const$5.set(seg, x);
    }

    public static int c_lflag$get(MemorySegment seg, long index) {
      return (int) constants$0.const$5.get(seg.asSlice(index * sizeof()));
    }

    public static void c_lflag$set(MemorySegment seg, long index, int x) {
      constants$0.const$5.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle c_line$VH() {
      return constants$1.const$0;
    }

    /**
     * Getter for field:
     * {@snippet :
     * cc_t c_line;
     * }
     */
    public static byte c_line$get(MemorySegment seg) {
      return (byte) constants$1.const$0.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * cc_t c_line;
     * }
     */
    public static void c_line$set(MemorySegment seg, byte x) {
      constants$1.const$0.set(seg, x);
    }

    public static byte c_line$get(MemorySegment seg, long index) {
      return (byte) constants$1.const$0.get(seg.asSlice(index * sizeof()));
    }

    public static void c_line$set(MemorySegment seg, long index, byte x) {
      constants$1.const$0.set(seg.asSlice(index * sizeof()), x);
    }

    public static MemorySegment c_cc$slice(MemorySegment seg) {
      return seg.asSlice(17, 32);
    }

    public static VarHandle c_ispeed$VH() {
      return constants$1.const$1;
    }

    /**
     * Getter for field:
     * {@snippet :
     * speed_t c_ispeed;
     * }
     */
    public static int c_ispeed$get(MemorySegment seg) {
      return (int) constants$1.const$1.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * speed_t c_ispeed;
     * }
     */
    public static void c_ispeed$set(MemorySegment seg, int x) {
      constants$1.const$1.set(seg, x);
    }

    public static int c_ispeed$get(MemorySegment seg, long index) {
      return (int) constants$1.const$1.get(seg.asSlice(index * sizeof()));
    }

    public static void c_ispeed$set(MemorySegment seg, long index, int x) {
      constants$1.const$1.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle c_ospeed$VH() {
      return constants$1.const$2;
    }

    /**
     * Getter for field:
     * {@snippet :
     * speed_t c_ospeed;
     * }
     */
    public static int c_ospeed$get(MemorySegment seg) {
      return (int) constants$1.const$2.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * speed_t c_ospeed;
     * }
     */
    public static void c_ospeed$set(MemorySegment seg, int x) {
      constants$1.const$2.set(seg, x);
    }

    public static int c_ospeed$get(MemorySegment seg, long index) {
      return (int) constants$1.const$2.get(seg.asSlice(index * sizeof()));
    }

    public static void c_ospeed$set(MemorySegment seg, long index, int x) {
      constants$1.const$2.set(seg.asSlice(index * sizeof()), x);
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
      return LibCHelper.asArray(addr, $LAYOUT(), 1, arena);
    }
  }

  public class pollfd {

	    pollfd() {
	        // Should not be called directly
	    }

	    private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(
	        ValueLayout.JAVA_INT.withName("fd"),
	        ValueLayout.JAVA_SHORT.withName("events"),
	        ValueLayout.JAVA_SHORT.withName("revents")
	    ).withName("pollfd");

	    /**
	     * The layout of this struct
	     */
	    public static final GroupLayout layout() {
	        return $LAYOUT;
	    }

	    private static final OfInt fd$LAYOUT = (OfInt)$LAYOUT.select(groupElement("fd"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * int fd
	     * }
	     */
	    public static final OfInt fd$layout() {
	        return fd$LAYOUT;
	    }

	    private static final long fd$OFFSET = 0;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * int fd
	     * }
	     */
	    public static final long fd$offset() {
	        return fd$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * int fd
	     * }
	     */
	    public static int fd(MemorySegment struct) {
	        return struct.get(fd$LAYOUT, fd$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * int fd
	     * }
	     */
	    public static void fd(MemorySegment struct, int fieldValue) {
	        struct.set(fd$LAYOUT, fd$OFFSET, fieldValue);
	    }

	    private static final OfShort events$LAYOUT = (OfShort)$LAYOUT.select(groupElement("events"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * short events
	     * }
	     */
	    public static final OfShort events$layout() {
	        return events$LAYOUT;
	    }

	    private static final long events$OFFSET = 4;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * short events
	     * }
	     */
	    public static final long events$offset() {
	        return events$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * short events
	     * }
	     */
	    public static short events(MemorySegment struct) {
	        return struct.get(events$LAYOUT, events$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * short events
	     * }
	     */
	    public static void events(MemorySegment struct, short fieldValue) {
	        struct.set(events$LAYOUT, events$OFFSET, fieldValue);
	    }

	    private static final OfShort revents$LAYOUT = (OfShort)$LAYOUT.select(groupElement("revents"));

	    /**
	     * Layout for field:
	     * {@snippet lang=c :
	     * short revents
	     * }
	     */
	    public static final OfShort revents$layout() {
	        return revents$LAYOUT;
	    }

	    private static final long revents$OFFSET = 6;

	    /**
	     * Offset for field:
	     * {@snippet lang=c :
	     * short revents
	     * }
	     */
	    public static final long revents$offset() {
	        return revents$OFFSET;
	    }

	    /**
	     * Getter for field:
	     * {@snippet lang=c :
	     * short revents
	     * }
	     */
	    public static short revents(MemorySegment struct) {
	        return struct.get(revents$LAYOUT, revents$OFFSET);
	    }

	    /**
	     * Setter for field:
	     * {@snippet lang=c :
	     * short revents
	     * }
	     */
	    public static void revents(MemorySegment struct, short fieldValue) {
	        struct.set(revents$LAYOUT, revents$OFFSET, fieldValue);
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
