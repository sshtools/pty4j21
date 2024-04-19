package com.pty4j.unix;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_BYTE;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemoryLayout.PathElement;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.StructLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;

import com.pty4j.Native;

public class LibC {
  
  private final static class LazyInit {
    private static final MethodHandle HNDL_WRITE = LibCHelper.downcallHandle("write", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT));
    private static final MethodHandle HNDL_READ = LibCHelper.downcallHandle("read", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT));
  }
  
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

  public static int getpid() {
    try {
      return (int) LibCHelper.downcallHandle("getpid", FunctionDescriptor.of(JAVA_INT)).invokeExact();
    } catch (RuntimeException re) {
      throw re;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  public static int getppid() {
    try {
      return (int) LibCHelper.downcallHandle("getppid", FunctionDescriptor.of(JAVA_INT)).invokeExact();
    } catch (RuntimeException re) {
      throw re;
    } catch (Throwable e) {
      throw new IllegalStateException(e);
    }
  }

  public static int chdir(MemorySegment __path) {
    try {
      return (int) LibCHelper.downcallHandle("chdir", FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER))
          .invokeExact(__path);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int setpgid(int __pid, int __pgid) {
    try {
      return (int) LibCHelper.downcallHandle("setpgid", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT))
          .invokeExact(__pid, __pgid);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int fork() {
    try {
      return (int) LibCHelper.downcallHandle("fork", FunctionDescriptor.of(JAVA_INT)).invokeExact();
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int close(int __fd) {
    try {
      return (int) LibCHelper.downcallHandle("close", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int setsid() {
    try {
      return (int) LibCHelper.downcallHandle("setsid", FunctionDescriptor.of(JAVA_INT)).invokeExact();
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int dup2(int __fd, int __fd2) {
    try {
      return (int) LibCHelper.downcallHandle("dup2", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT))
          .invokeExact(__fd, __fd2);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int waitpid(int __pid, MemorySegment __stat_loc, int __options) {
    try {
      return (int) LibCHelper
          .downcallHandle("waitpid", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, JAVA_INT))
          .invokeExact(__pid, __stat_loc, __options);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int killpg(int __pgrp, int __sig) {
    try {
      return (int) LibCHelper.downcallHandle("killpg", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT))
          .invokeExact(__pgrp, __sig);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int kill(int __pid, int __sig) {
    try {
      return (int) LibCHelper.downcallHandle("kill", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT))
          .invokeExact(__pid, __sig);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int read(int __fd, MemorySegment __buf, int __len) {
    try {
      return (int) LazyInit.HNDL_READ.invokeExact(__fd, __buf, __len);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int write(int __fd, MemorySegment __buf, int __len) {
    try {
      return (int) LazyInit.HNDL_WRITE
          .invokeExact(__fd, __buf, __len);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int sigprocmask(int __how, MemorySegment __set, MemorySegment __oset) {
    try {
      return (int) LibCHelper
          .downcallHandle("sigprocmask",
              FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, LibCHelper.POINTER))
          .invokeExact(__how, __set, __oset);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int grantpt(int __fdm) {
    try {
      return (int) LibCHelper.downcallHandle("grantpt", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(__fdm);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int unlockpt(int __fdm) {
    try {
      return (int) LibCHelper.downcallHandle("unlockpt", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(__fdm);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static MemorySegment ptsname(int __fd) {
    try {
      return (java.lang.foreign.MemorySegment) LibCHelper
          .downcallHandle("ptsname", FunctionDescriptor.of(LibCHelper.POINTER, JAVA_INT)).invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int unsetenv(MemorySegment __name) {
    try {
      return (int) LibCHelper.downcallHandle("unsetenv", FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER))
          .invokeExact(__name);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int select(int __nfds, MemorySegment __readfds, MemorySegment __writefds, MemorySegment __exceptfds,
      MemorySegment __timeout) {
    try {
      return (int) LibCHelper
          .downcallHandle("select", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER, LibCHelper.POINTER,
              LibCHelper.POINTER, LibCHelper.POINTER))
          .invokeExact(__nfds, __readfds, __writefds, __exceptfds, __timeout);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static MemorySegment strerror(int __errno) {
    try {
      return (java.lang.foreign.MemorySegment) LibCHelper
          .downcallHandle("strerror", FunctionDescriptor.of(LibCHelper.POINTER, JAVA_INT)).invokeExact(__errno);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int pipe(MemorySegment __pipedes) {
    try {
      return (int) LibCHelper.downcallHandle("pipe", FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER))
          .invokeExact(__pipedes);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int open(MemorySegment __file, int __oflag, Object... x2) {
    try {
      return (int) LibCHelper
          .downcallHandleVariadic("open", FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, JAVA_INT))
          .invokeExact(__file, __oflag, x2);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int tcdrain(int __fd) {
    try {
      return (int) LibCHelper.downcallHandle("tcdrain", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(__fd);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int poll(MemorySegment __fds, long __nfds, int __timeout) {
    try {
      return (int) LibCHelper
          .downcallHandle("poll", FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, JAVA_LONG, JAVA_INT))
          .invokeExact(__fds, __nfds, __timeout);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int tcsetattr(int __fd, int __optional_actions, MemorySegment __termios_p) {
    try {
      return (int) LibCHelper
          .downcallHandle("tcsetattr", FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, LibCHelper.POINTER))
          .invokeExact(__fd, __optional_actions, __termios_p);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public static int tcgetattr(int __fd, MemorySegment __termios_p) {
    try {
      return (int) LibCHelper.downcallHandle("tcgetattr", FunctionDescriptor.of(JAVA_INT, JAVA_INT, LibCHelper.POINTER))
          .invokeExact(__fd, __termios_p);
    } catch (Throwable ex$) {
      throw new AssertionError("should not reach here", ex$);
    }
  }

  public final static class LibCHelper {

    private static final SymbolLookup SYMBOL_LOOKUP;
    public static final AddressLayout POINTER = ValueLayout.ADDRESS
        .withTargetLayout(MemoryLayout.sequenceLayout(JAVA_BYTE));
    

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

  public final static class fd_set {

    static final StructLayout const$2 = MemoryLayout
        .structLayout(MemoryLayout.sequenceLayout(16, JAVA_LONG).withName("fds_bits")).withName("");

    public static MemoryLayout $LAYOUT() {
      return const$2;
    }

    public static MemorySegment fds_bits$slice(MemorySegment seg) {
      return seg.asSlice(0, 128);
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

  public final static class pollfd {

    final static class constants$0 {

      // Suppresses default constructor, ensuring non-instantiability.
      private constants$0() {
      }

      static final StructLayout const$0 = MemoryLayout
          .structLayout(JAVA_INT.withName("fd"), JAVA_SHORT.withName("events"), JAVA_SHORT.withName("revents"))
          .withName("pollfd");
      static final VarHandle const$1 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("fd"));
      static final VarHandle const$2 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("events"));
      static final VarHandle const$3 = constants$0.const$0.varHandle(MemoryLayout.PathElement.groupElement("revents"));
      static final FunctionDescriptor const$4 = FunctionDescriptor.of(JAVA_INT, LibCHelper.POINTER, JAVA_LONG,
          JAVA_INT);
      static final MethodHandle const$5 = LibCHelper.downcallHandle("poll", constants$0.const$4);
    }

    public static MemoryLayout $LAYOUT() {
      return constants$0.const$0;
    }

    public static VarHandle fd$VH() {
      return constants$0.const$1;
    }

    /**
     * Getter for field:
     * {@snippet :
     * int fd;
     * }
     */
    public static int fd$get(MemorySegment seg) {
      return (int) constants$0.const$1.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * int fd;
     * }
     */
    public static void fd$set(MemorySegment seg, int x) {
      constants$0.const$1.set(seg, x);
    }

    public static int fd$get(MemorySegment seg, long index) {
      return (int) constants$0.const$1.get(seg.asSlice(index * sizeof()));
    }

    public static void fd$set(MemorySegment seg, long index, int x) {
      constants$0.const$1.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle events$VH() {
      return constants$0.const$2;
    }

    /**
     * Getter for field:
     * {@snippet :
     * short events;
     * }
     */
    public static short events$get(MemorySegment seg) {
      return (short) constants$0.const$2.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * short events;
     * }
     */
    public static void events$set(MemorySegment seg, short x) {
      constants$0.const$2.set(seg, x);
    }

    public static short events$get(MemorySegment seg, long index) {
      return (short) constants$0.const$2.get(seg.asSlice(index * sizeof()));
    }

    public static void events$set(MemorySegment seg, long index, short x) {
      constants$0.const$2.set(seg.asSlice(index * sizeof()), x);
    }

    public static VarHandle revents$VH() {
      return constants$0.const$3;
    }

    /**
     * Getter for field:
     * {@snippet :
     * short revents;
     * }
     */
    public static short revents$get(MemorySegment seg) {
      return (short) constants$0.const$3.get(seg);
    }

    /**
     * Setter for field:
     * {@snippet :
     * short revents;
     * }
     */
    public static void revents$set(MemorySegment seg, short x) {
      constants$0.const$3.set(seg, x);
    }

    public static short revents$get(MemorySegment seg, long index) {
      return (short) constants$0.const$3.get(seg.asSlice(index * sizeof()));
    }

    public static void revents$set(MemorySegment seg, long index, short x) {
      constants$0.const$3.set(seg.asSlice(index * sizeof()), x);
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

}