/*
 * JPty - A small PTY interface for Java.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.pty4j.unix;

import java.io.File;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.pty4j.Platform;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.pty4j.unix.LibC.LibCHelper;
import com.pty4j.unix.LibC.pollfd;
import com.pty4j.unix.LibC.termios;
import com.pty4j.util.LazyValue;
import com.pty4j.util.PtyUtil;

/**
 * Provides access to the pseudoterminal functionality on POSIX(-like) systems,
 * emulating such system calls on non POSIX systems.
 */
public class PtyHelpers {
  private static final Logger LOG = System.getLogger(PtyHelpers.class.getName());
  
  public interface FDSet {
    void set(int fd);
    boolean isSet(int fd);
    MemorySegment memory();
  }

  /**
   * Provides a OS-specific interface to the PtyHelpers methods.
   */
  public interface OSFacade {
    /**
     * Terminates or signals the process with the given PID.
     *
     * @param pid the process ID to terminate or signal;
     * @param signal the signal number to send, for example, 9 to terminate the
     *            process.
     * @return a value of <code>0</code> upon success, or a non-zero value in case
     *         of an error (see {@link PtyHelpers#errno()} for details).
     */
    default int kill(int pid, int signal) {
       return LibC.kill(pid, signal);
    }

    /**
     * Waits until the process with the given PID is stopped.
     *
     * @param pid     the PID of the process to wait for;
     * @param stat    the array in which the result code of the process will be
     *                stored;
     * @param options the options for waitpid (not used at the moment).
     * @return 0 upon success, -1 upon failure (see {@link PtyHelpers#errno()} for
     *         details).
     */
    default int waitpid(int pid, int[] stat, int options) {
      try (var offHeap = Arena.ofConfined()) {
          return LibC.waitpid(pid, offHeap.allocateFrom(ValueLayout.JAVA_INT, stat), options);
      }
    }

    default int sigprocmask(int how, AtomicInteger set, AtomicInteger oldset) {
        try (var offHeap = Arena.ofConfined()) {
        	var setMem = offHeap.allocate(ValueLayout.JAVA_INT, set.get());
        	var oldsetMem = offHeap.allocate(ValueLayout.JAVA_INT, oldset.get());
            var res = LibC.sigprocmask(how, setMem, oldsetMem);
            set.set(setMem.get(ValueLayout.JAVA_INT, 0));
            oldset.set(oldsetMem.get(ValueLayout.JAVA_INT, 0));
            return res;
        }
    }
    
    default String strerror(int errno) {
      return LibC.strerror(errno).getString(0);
    }

    int getpt(); //getpt

    default int grantpt(int fd) {
      return LibC.grantpt(fd);
    }

    default int unlockpt(int fd) {
      return LibC.unlockpt(fd);
    }

    default int close(int fd) {
      return LibC.close(fd);
    }

    default String ptsname(int fd) {
      var seg = LibC.ptsname(fd);
      if(seg.equals(MemorySegment.NULL))
    	  return null;
      else
    	  return seg.getString(0);
    }

    default int killpg(int pid, int sig) {
        return LibC.killpg(pid, sig);
    }

    default int fork() {
        return LibC.fork();
    }

    default int pipe(int[] pipe2) {
      try (var offHeap = Arena.ofConfined()) {
        var arr = offHeap.allocateFrom(ValueLayout.JAVA_INT, pipe2);
        var res = LibC.pipe(arr);
        if(res == 0) {
          var ret = arr.toArray(ValueLayout.JAVA_INT);
          System.arraycopy(ret, 0, pipe2, 0, ret.length);
        }
        return res;
      }
    }

    default int setsid() {
      return LibC.setsid();
    }

    default int getpid() {
      return LibC.getpid();
    }

    default int setpgid(int pid, int pgid) {
      return LibC.setpgid(pid, pgid);
    }

    default void dup2(int fds, int fileno) {
  	  LibC.dup2(fds, fileno);
    }

    default int getppid() {
      return LibC.getppid();
    }

    default void unsetenv(String s) {
      try (var offHeap = Arena.ofConfined()) {  
      	LibC.unsetenv(offHeap.allocateFrom(s));
      }
    }

    int login_tty(int fd);

    default void chdir(String dirpath) {
      try (var offHeap = Arena.ofConfined()) {
      	LibC.chdir(offHeap.allocateFrom(dirpath));
      }
    }

    default int tcdrain(int fd) {
  	  return LibC.tcdrain(fd);
    }

    default int open(String path, int mode) {
      try (var offHeap = Arena.ofConfined()) {
        return LibC.open(offHeap.allocateFrom(path), mode);
      }
    }

    default int fastRead(int fd, MemorySegment buffer, int len) {
      return LibC.read(fd, buffer, len);
    }

    default int read(int fd, byte[] buffer, int len) {
        try (var offHeap = Arena.ofConfined()) {
          var arr = offHeap.allocate(len);
          var res = LibC.read(fd, arr, len);
          if(res > 0) {
              arr.asByteBuffer().get(buffer);
          }
          return res;
        }
    }

    default int write(int fd, byte[] buffer, int len) {
        try (var offHeap = Arena.ofConfined()) {
          return LibC.write(fd, offHeap.allocateFrom(ValueLayout.JAVA_BYTE, buffer), len);
        }
    }

    default int tcgetattr(int fd, TerminalSettings settings) {
        try (var offHeap = Arena.ofConfined()) {
          var termiosMem = termios.allocate(offHeap);
          var result = LibC.tcgetattr(fd, termiosMem);
          fillTerminalSettings(settings, termiosMem);
          return result;
        }
    }

    default int tcsetattr(int fd, int opt, TerminalSettings settings) {
      try (var offHeap = Arena.ofConfined()) {
        return LibC.tcsetattr(fd, opt, convertToTermios(settings, offHeap));
      }
    }

    default int poll(int[] fds, short[] events, long nfds, int timeout, AtomicInteger errno) {
      /* https://docs.oracle.com/en/java/javase/21/core/checking-native-errors-using-errno.html#GUID-BBB64F4A-A68C-4E70-BFCA-B9984D7D3C47 */
      try (var offHeap = Arena.ofConfined()) {
        var capturedState = offHeap.allocate(LibCHelper.capturedStateLayout);
        var pollfds = pollfd.allocateArray(fds.length, offHeap);
        for(int i = 0 ; i < fds.length; i++) {
          var pfd = pollfd.asSlice(pollfds, i);
      	  pollfd.fd(pfd, fds[i]);
          pollfd.events(pfd, events[i]);
        }
  	    var res = LibC.poll(pollfds, nfds, timeout);
  	    if(res < 1) {
  	      var errnoVal = (int)LibCHelper.errnoHandle.get(capturedState);
		  errno.set(errnoVal);
  	    }
  	    else {
            for(int i = 0 ; i < fds.length; i++) {
              var fd = pollfd.asSlice(pollfds, i);
              events[i] = pollfd.revents(fd);
            }
  	    }
  	    return res;
      }
    }
//
//    default int errno() {
//      return JTermios.errno();
//    }
  }

  public static class TerminalSettings {
    public int c_iflag;
    public int c_oflag;
    public int c_cflag;
    public int c_lflag;
    public byte[] c_cc = new byte[20];
    public int c_ispeed;
    public int c_ospeed;
  }

  private static MemorySegment convertToTermios(TerminalSettings settings, SegmentAllocator allocator) {
	var result = termios.allocate(allocator);
	termios.c_iflag$set(result, settings.c_iflag);
	termios.c_oflag$set(result, settings.c_oflag);
	termios.c_cflag$set(result, settings.c_cflag);
	termios.c_lflag$set(result, settings.c_lflag);
	termios.c_cc$slice(result).asByteBuffer().put(settings.c_cc);
	termios.c_ispeed$set(result, settings.c_ispeed);
	termios.c_ospeed$set(result, settings.c_ospeed);
    return result;
  }

  private static void fillTerminalSettings(TerminalSettings settings, MemorySegment termiosMem) {
	settings.c_iflag = termios.c_iflag$get(termiosMem);
	settings.c_oflag = termios.c_oflag$get(termiosMem);
	settings.c_cflag = termios.c_cflag$get(termiosMem);
	settings.c_lflag = termios.c_lflag$get(termiosMem);
	settings.c_cc = termios.c_cc$slice(termiosMem).toArray(ValueLayout.JAVA_BYTE);
    settings.c_ispeed = termios.c_ispeed$get(termiosMem);
    settings.c_ospeed = termios.c_ospeed$get(termiosMem);
  }

  // CONSTANTS

  public static int ONLCR = 0x04;

  public static int VINTR = 0;
  public static int VQUIT = 1;
  public static int VERASE = 2;
  public static int VKILL = 3;
  public static int VSUSP = 10;
  public static int VREPRINT = 12;
  public static int VWERASE = 14;

  public static int ECHOCTL = 0x1000;
  public static int ECHOKE = 0x4000;
  public static int ECHOK = 0x00000004;

  public static int IMAXBEL = 0x00002000;
  public static int HUPCL = 0x00004000;

  public static int IUTF8 = 0x00004000;

  private static final int STDIN_FILENO = 0;
  private static final int STDOUT_FILENO = 1;
  private static final int STDERR_FILENO = 2;


  /*
 * Flags for sigprocmask:
 */
  private static final int SIG_UNBLOCK = 2;

  public static int SIGHUP = 1;
  public static int SIGINT = 2;
  public static int SIGQUIT = 3;
  public static int SIGILL = 4;
  public static int SIGABORT = 6;
  public static int SIGFPE = 8;
  public static int SIGKILL = 9;
  public static int SIGSEGV = 11;
  public static int SIGPIPE = 13;
  public static int SIGALRM = 14;
  public static int SIGTERM = 15;
  public static int SIGCHLD = 20;

  public static int WNOHANG = 1;
  public static int WUNTRACED = 2;

  private static final LazyValue<OSFacade> OS_FACADE_VALUE = new LazyValue<>(() -> {
    if (Platform.isMac()) {
      return new com.pty4j.unix.macosx.OSFacadeImpl();
    }
    if (Platform.isFreeBSD()) {
      return new com.pty4j.unix.freebsd.OSFacadeImpl();
    }
    if (Platform.isOpenBSD()) {
      return new com.pty4j.unix.openbsd.OSFacadeImpl();
    }
    if (Platform.isLinux() || Platform.isAndroid()) {
      return new com.pty4j.unix.linux.OSFacadeImpl();
    }
    if (Platform.isWindows()) {
      throw new IllegalArgumentException("WinPtyProcess should be used on Windows");
    }
    throw new RuntimeException("Pty4J has no support for OS " + System.getProperty("os.name"));
  });

  private static final LazyValue<PtyExecutor> PTY_EXECUTOR_VALUE = new LazyValue<>(() -> {
    String libraryName = Platform.isMac() ? "libpty.dylib" : "libpty.so";
    File libraryFile = PtyUtil.resolveNativeFile(libraryName);
    return new NativePtyExecutor(libraryFile.getAbsolutePath());
  });

  static {
    try {
      getOsFacade();
    }
    catch (Throwable t) {
      LOG.log(Level.ERROR, t.getMessage(), t.getCause());
    }
    try {
      getPtyExecutor();
    }
    catch (Throwable t) {
      LOG.log(Level.ERROR, t.getMessage(), t.getCause());
    }
  }

  @NotNull
  private static OSFacade getOsFacade() {
    try {
      return OS_FACADE_VALUE.getValue();
    }
    catch (Throwable t) {
      throw new RuntimeException("Cannot load implementation of " + OSFacade.class, t);
    }
  }

  @NotNull static PtyExecutor getPtyExecutor() {
    try {
      return PTY_EXECUTOR_VALUE.getValue();
    }
    catch (Throwable t) {
      throw new RuntimeException("Cannot load native pty executor library", t);
    }
  }

  public static OSFacade getInstance() {
    return getOsFacade();
  }

  public static MemorySegment createTermios(SegmentAllocator allocator) {
	var term = termios.allocate(allocator);

    boolean isUTF8 = true;
    termios.c_iflag$set(term, LibC.ICRNL | LibC.IXON | LibC.IXANY | IMAXBEL | LibC.BRKINT | (isUTF8 ? IUTF8 : 0));
    termios.c_oflag$set(term, LibC.OPOST | LibC.ONLCR);
    termios.c_cflag$set(term, LibC.CREAD | LibC.CS8 | HUPCL);
    termios.c_lflag$set(term, LibC.ICANON | LibC.ISIG | LibC.IEXTEN | LibC.ECHO | LibC.ECHOE | ECHOK | ECHOKE | ECHOCTL);

    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VEOF, CTRLKEY('D'));
//    term.c_cc[VEOL] = -1;
//    term.c_cc[VEOL2] = -1;
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VERASE, (byte)0x7f);           // DEL
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VWERASE, CTRLKEY('W'));
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VKILL, CTRLKEY('U'));
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VREPRINT, CTRLKEY('R'));
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VINTR, CTRLKEY('C'));
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VQUIT, (byte)0x1c);           // Control+backslash
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VSUSP, CTRLKEY('Z'));
//    term.c_cc[VDSUSP] = CTRLKEY('Y');
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VSTART, CTRLKEY('Q'));
    termios.c_cc$slice(term).set(ValueLayout.JAVA_BYTE, LibC.VSTOP, CTRLKEY('S'));
//    term.c_cc[VLNEXT] = CTRLKEY('V');
//    term.c_cc[VDISCARD] = CTRLKEY('O');
//    term.c_cc[VMIN] = 1;
//    term.c_cc[VTIME] = 0;
//    term.c_cc[VSTATUS] = CTRLKEY('T');

    termios.c_ispeed$set(term, LibC.B38400);
    termios.c_ospeed$set(term, LibC.B38400);

    return term;
  }

  private static byte CTRLKEY(char c) {
    return (byte)((byte)c - (byte)'A' + 1);
  }

  private static int __sigbits(int __signo) {
    return __signo > 32 ? 0 : (1 << (__signo - 1));
  }

  public static @NotNull WinSize getWinSize(int fd, @Nullable PtyProcess process) throws UnixPtyException {
    return getPtyExecutor().getWindowSize(fd, process);
  }

  /**
   * Tests whether the process with the given process ID is alive or terminated.
   *
   * @param pid the process-ID to test.
   * @return <code>true</code> if the process with the given process ID is
   *         alive, <code>false</code> if it is terminated.
   */
  public static boolean isProcessAlive(int pid) {
    int[] stat = {-1};
    int result = PtyHelpers.waitpid(pid, stat, WNOHANG);
    return (result == 0) && (stat[0] < 0);
  }

  /**
   * Terminates or signals the process with the given PID.
   *
   * @param pid    the process ID to terminate or signal;
   * @param signal the signal number to send, for example, 9 to terminate the
   *               process.
   * @return a value of <code>0</code> upon success, or a non-zero value in case of
   *         an error.
   */
  public static int signal(int pid, int signal) {
    return getOsFacade().kill(pid, signal);
  }

  /**
   * Blocks and waits until the given PID either terminates, or receives a
   * signal.
   *
   * @param pid     the process ID to wait for;
   * @param stat    an array of 1 integer in which the status of the process is
   *                stored;
   * @param options the bit mask with options.
   */
  public static int waitpid(int pid, int[] stat, int options) {
    return getOsFacade().waitpid(pid, stat, options);
  }

  public static void chdir(String dirpath) {
    getOsFacade().chdir(dirpath);
  }

  public static int execPty(String full_path,
                            String[] argv,
                            String[] envp,
                            String dirpath,
                            String pts_name,
                            int fdm,
                            String err_pts_name,
                            int err_fdm,
                            boolean console) {
    PtyExecutor executor = getPtyExecutor();
    return executor.execPty(full_path, argv, envp, dirpath, pts_name, fdm, err_pts_name, err_fdm, console);
  }
}
