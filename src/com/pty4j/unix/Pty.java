/*
 * Copyright (c) 2002, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package com.pty4j.unix;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.MemorySegment;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.pty4j.util.Pair;


/**
 * Pty - pseudo terminal support.
 */
public final class Pty {
  private static final Logger LOG = System.getLogger(Pty.class.getName());
	  
  private static final int O_RDONLY = 0x0000;
  private final String mySlaveName;
  private final PTYInputStream myIn;
  private final PTYOutputStream myOut;
  private final Object myFDLock = new Object();
  private final Object mySelectLock = new Object();
  private final int[] myPipe = new int[2];
  private final PtyHelpers.OSFacade ptyHelper;

  private volatile int myMaster;
  private volatile int mySlaveFD;

  private static final Object PTSNAME_LOCK = new Object();

  public Pty() throws IOException {
    this(false, false);
  }

  /**
   * @deprecated use {@link #Pty()} instead
   */
  @Deprecated(forRemoval = true)
  public Pty(boolean console) throws IOException {
    this(console, false);
  }

  Pty(@SuppressWarnings("unused") boolean console,
      boolean openOpenTtyToPreserveOutputAfterTermination) throws IOException {
    Pair<Integer, String> masterSlave = openMaster();
    myMaster = masterSlave.getFirst();
    mySlaveName = masterSlave.getSecond();
    
    LOG.log(Level.INFO, "Created pty {0} for master fd {1}, console is {2}", mySlaveName, myMaster, console);

    if (mySlaveName == null) {
      throw new IOException("Util.exception.cannotCreatePty");
    }
    
    ptyHelper = PtyHelpers.getInstance();

    // Without this line, on macOS the slave side of the pty will be automatically closed on process termination, and it
    // will be impossible to read process output after exit. It has a side effect: the child process won't be terminated
    // until we've read all the output from it.
    //
    // See this report for details: https://developer.apple.com/forums/thread/663632
    mySlaveFD = openOpenTtyToPreserveOutputAfterTermination ? ptyHelper.open(mySlaveName, O_RDONLY) : -1;

    myIn = new PTYInputStream(this);
    myOut = new PTYOutputStream(this);

    ptyHelper.pipe(myPipe);
  }

  public String getSlaveName() {
    return mySlaveName;
  }

  public int getMasterFD() {
    return myMaster;
  }

  public PTYOutputStream getOutputStream() {
    return myOut;
  }

  public PTYInputStream getInputStream() {
    return myIn;
  }

  /**
   * Change terminal window size to given width and height.
   * <p>
   * This should only be used when the pseudo terminal is configured for use with a terminal emulation, i.e. when
   * {@link UnixPtyProcess#isConsoleMode()} returns {@code false}.
   *
   * @param winSize new window size
   */
  public void setWindowSize(@NotNull WinSize winSize, @Nullable PtyProcess process) throws UnixPtyException {
    PtyHelpers.getPtyExecutor().setWindowSize(myMaster, winSize, process);
  }


  /**
   * Returns the current window size of this Pty.
   *
   * @return a {@link com.pty4j.WinSize} instance with information about the master sid of the Pty.
   * @throws UnixPtyException in case obtaining the window size failed.
   */
  public @NotNull WinSize getWinSize(@Nullable PtyProcess process) throws UnixPtyException {
    return PtyHelpers.getPtyExecutor().getWindowSize(myMaster, process);
  }

  /**
   * Creates a pty pair (master file descriptor and slave path).
   * If creation fails, the master file descriptor is negative.
   * @return the created pty pair
   */
  public static Pair<Integer, String> ptyMasterOpen() {

    PtyHelpers.OSFacade m_jpty = PtyHelpers.getInstance();

    String name = "/dev/ptmx";

    int fdm = m_jpty.getpt();

    if (fdm < 0) {
      return new Pair<>(-1, name);
    }
    if (m_jpty.grantpt(fdm) < 0) { /* grant access to slave */
      m_jpty.close(fdm);
      return new Pair<>(-2, name);
    }
    if (m_jpty.unlockpt(fdm) < 0) { /* clear slave's lock flag */
      m_jpty.close(fdm);
      return new Pair<>(-3, name);
    }

    String ptr = ptsname(m_jpty, fdm);

    if (ptr == null) { /* get slave's name */
      m_jpty.close(fdm);
      return new Pair<>(-4, name);
    }
    return new Pair<>(fdm, ptr);
  }

  private static String ptsname(PtyHelpers.OSFacade m_jpty, int fdm) {
    synchronized (PTSNAME_LOCK) {
      // ptsname() function is not thread-safe: http://man7.org/linux/man-pages/man3/ptsname.3.html
      return m_jpty.ptsname(fdm);
    }
  }


  private Pair<Integer, String> openMaster() {
    return ptyMasterOpen();
  }

  public static int raise(int pid, int sig) {
    PtyHelpers.OSFacade m_jpty = PtyHelpers.getInstance();

    int status = m_jpty.killpg(pid, sig);

    if (status == -1) {
      status = m_jpty.kill(pid, sig);
    }

    return status;
  }

  public boolean isClosed() {
    return myMaster == -1;
  }

  public void close() throws IOException {
    if (myMaster != -1) {
      synchronized (myFDLock) {
        if (myMaster != -1) {
          int fd = myMaster;
          myMaster = -1;
          int status = close0(fd);
          if (status == -1) {
            throw new IOException("Close error");
          }
        }
      }
    }

    if (mySlaveFD != -1) {
      synchronized (myFDLock) {
        if (mySlaveFD != -1) {
          int fd = mySlaveFD;
          mySlaveFD = -1;
          int status = ptyHelper.close(fd);
          if (status == -1) {
            throw new IOException("Close error");
          }
        }
      }
    }
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  private int close0(int fd) throws IOException {
    int ret = ptyHelper.close(fd);

    breakRead();

    synchronized (mySelectLock) {
      ptyHelper.close(myPipe[0]);
      ptyHelper.close(myPipe[1]);
      myPipe[0] = -1;
      myPipe[1] = -1;
    }

    return ret;
  }

  void breakRead() {
	  ptyHelper.write(myPipe[1], new byte[1], 1);
  }

  int fastRead(MemorySegment buf, int len) {
    int fd = myMaster;
    if (fd == -1) return -1;

    boolean haveBytes;
    synchronized (mySelectLock) {
      if (myPipe[0] == -1) return -1;

      haveBytes = poll(myPipe[0], fd);
    }

    return haveBytes ? ptyHelper.fastRead(fd, buf, len) : -1;
  }

  int read(byte[] buf, int len) throws IOException {
    int fd = myMaster;
    if (fd == -1) return -1;

    boolean haveBytes;
    synchronized (mySelectLock) {
      if (myPipe[0] == -1) return -1;

      haveBytes = poll(myPipe[0], fd);
    }

    return haveBytes ? ptyHelper.read(fd, buf, len) : -1;
  }

  private boolean poll(int pipeFd, int fd) {
    var poll_fds = new int[]{pipeFd, fd};
    var poll_evts = new short[]{(short)LibC.POLLIN, (short)LibC.POLLIN};
    var errno = new AtomicInteger();
    while (true) {
      if (ptyHelper.poll(poll_fds, poll_evts, 2, -1, errno) > 0) 
    	  break;

      if (errno.get() != LibC.EAGAIN && errno.get() != LibC.EINTR) 
    	  return false;
    }
    return ((poll_evts[1]) & LibC.POLLIN) != 0;
  }

  int write(byte[] buf, int len) throws IOException {
    return ptyHelper.write(myMaster, buf, len); 
  }

}
