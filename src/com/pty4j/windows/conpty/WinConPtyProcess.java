package com.pty4j.windows.conpty;

import static com.pty4j.Native.err;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.jetbrains.annotations.NotNull;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessOptions;
import com.pty4j.WinSize;
import com.pty4j.windows.Kernel32;
import com.pty4j.windows.Kernel32.PROCESS_INFORMATION;
import com.pty4j.windows.WinHelper;

public final class WinConPtyProcess extends PtyProcess {
  private static final Logger LOG = System.getLogger(WinConPtyProcess.class.getName());

  private final PseudoConsole pseudoConsole;
  private final MemorySegment processInformation;
  private final WinHandleInputStream myInputStream;
  private final WinHandleOutputStream myOutputStream;
  private final ExitCodeInfo myExitCodeInfo = new ExitCodeInfo();
  private final List<String> myCommand;
  private final Arena mem;

  public WinConPtyProcess(@NotNull PtyProcessOptions options) throws IOException {
    myCommand = List.of(options.getCommand());
    checkExec(myCommand);
    Pipe inPipe = new Pipe();
    Pipe outPipe = new Pipe();
    mem = Arena.ofShared();
    pseudoConsole = new PseudoConsole(getInitialSize(options), inPipe.getReadPipe(), outPipe.getWritePipe());
    processInformation = ProcessUtils.startProcess(mem, pseudoConsole, options.getCommand(), options.getDirectory(),
            options.getEnvironment());
    if (err(Kernel32.CloseHandle(inPipe.getReadPipe()))) {
      throw new LastErrorExceptionEx("CloseHandle stdin after process creation");
    }
    if (err(Kernel32.CloseHandle(outPipe.getWritePipe()))) {
      throw new LastErrorExceptionEx("CloseHandle stdout after process creation");
    }
    myInputStream = new WinHandleInputStream(outPipe.getReadPipe());
    myOutputStream = new WinHandleOutputStream(inPipe.getWritePipe());
    startAwaitingThread(List.of(options.getCommand()));
  }

  private static void checkExec(@NotNull List<String> command) {
    String exec = command.size() > 0 ? command.get(0) : null;
    SecurityManager s = System.getSecurityManager();
    if (s != null && exec != null) {
      s.checkExec(exec);
    }
  }

  public @NotNull List<String> getCommand() {
    return myCommand;
  }

  private static @NotNull WinSize getInitialSize(@NotNull PtyProcessOptions options) {
    return new WinSize(Objects.requireNonNullElse(options.getInitialColumns(), 80),
        Objects.requireNonNullElse(options.getInitialRows(), 25));
  }

  private void startAwaitingThread(@NotNull List<String> command) {
    String commandLine = String.join(" ", command);
    Thread t = new Thread(() -> {
      int result = Kernel32.WaitForSingleObject(PROCESS_INFORMATION.hProcess(processInformation), Kernel32.INFINITE);
      int exitCode = -100;
      if (result == Kernel32.WAIT_OBJECT_0) {
    	try(Arena lmem = Arena.ofConfined()) {
	        MemorySegment exitCodeRef = lmem.allocate(ValueLayout.JAVA_INT);
	        if (err(Kernel32.GetExitCodeProcess(PROCESS_INFORMATION.hProcess(processInformation), exitCodeRef))) {
	          LOG.log(Level.INFO, "{0}", LastErrorExceptionEx.getErrorMessage("GetExitCodeProcess(" + commandLine + ")"));
	        } else {
	          exitCode = exitCodeRef.get(ValueLayout.JAVA_INT, 0);
	        }
    	}
      } else {
        if (result == Kernel32.WAIT_FAILED) {
          LOG.log(Level.INFO, "{0}", LastErrorExceptionEx.getErrorMessage("WaitForSingleObject(" + commandLine + ")"));
        } else {
          LOG.log(Level.INFO, "WaitForSingleObject({0}) returned {1}", commandLine, result);
        }
      }
      myExitCodeInfo.setExitCode(exitCode);
      myInputStream.awaitAvailableOutputIsRead();
      cleanup();
    }, "WinConPtyProcess WaitFor " + commandLine);
    t.setDaemon(true);
    t.start();
  }

  @Override
  public void setWinSize(@NotNull WinSize winSize) {
    try {
      pseudoConsole.resize(winSize);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public @NotNull WinSize getWinSize() throws IOException {
    return pseudoConsole.getWinSize();
  }

  @Override
  public long pid() {
    return PROCESS_INFORMATION.dwProcessId(processInformation);
  }

  @Override
  public OutputStream getOutputStream() {
    return myOutputStream;
  }

  @Override
  public InputStream getInputStream() {
    return myInputStream;
  }

  @Override
  public InputStream getErrorStream() {
    return InputStream.nullInputStream();
  }

  @Override
  public int waitFor() throws InterruptedException {
    return myExitCodeInfo.waitFor();
  }

  @Override
  public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
    return myExitCodeInfo.waitFor(timeout, unit);
  }

  @Override
  public int exitValue() {
    Integer exitCode = myExitCodeInfo.getExitCodeNow();
    if (exitCode != null) {
      return exitCode;
    }
    throw new IllegalThreadStateException("Process is still alive");
  }

  @Override
  public boolean isAlive() {
    return myExitCodeInfo.getExitCodeNow() == null;
  }

  @Override
  public boolean supportsNormalTermination() {
    return false;
  }

  @Override
  public void destroy() {
    if (!isAlive()) {
      return;
    }
    if (err(Kernel32.TerminateProcess(PROCESS_INFORMATION.hProcess(processInformation), 1))) {
      LOG.log(Level.INFO, "Failed to terminate process with pid {0}. {1}", PROCESS_INFORMATION.dwProcessId(processInformation),
          LastErrorExceptionEx.getErrorMessage("TerminateProcess"));
    }
  }

  public @NotNull String getWorkingDirectory() throws IOException {
    return WinHelper.getCurrentDirectory(pid());
  }

  public int getConsoleProcessCount() throws IOException {
    return ConsoleProcessListFetcher.getConsoleProcessCount(pid());
  }

  private void cleanup() {
	try {
	    try {
	      ProcessUtils.closeHandles(processInformation);
	    } catch (IOException e) {
	      e.printStackTrace();
	    }
	    pseudoConsole.close();
	    try {
	      myInputStream.close();
	    } catch (IOException e) {
	      LOG.log(Level.WARNING, "Cannot close input stream", e);
	    }
	    try {
	      myOutputStream.close();
	    } catch (IOException e) {
	    	LOG.log(Level.WARNING,"Cannot close output stream", e);
	    }
	}
	finally {
		mem.close();
	}
  }

  private static class ExitCodeInfo {
    private Integer myExitCode = null;
    private final ReentrantLock myLock = new ReentrantLock();
    private final Condition myCondition = myLock.newCondition();

    public void setExitCode(int exitCode) {
      myLock.lock();
      try {
        myExitCode = exitCode;
        myCondition.signalAll();
      } finally {
        myLock.unlock();
      }
    }

    public int waitFor() throws InterruptedException {
      myLock.lock();
      try {
        while (myExitCode == null) {
          myCondition.await();
        }
        return myExitCode;
      } finally {
        myLock.unlock();
      }
    }

    Integer getExitCodeNow() {
      myLock.lock();
      try {
        return myExitCode;
      } finally {
        myLock.unlock();
      }
    }

    public boolean waitFor(long timeout, TimeUnit unit) throws InterruptedException {
      long startTime = System.nanoTime();
      long remaining = unit.toNanos(timeout);
      myLock.lock();
      try {
        while (myExitCode == null && remaining > 0) {
          //noinspection ResultOfMethodCallIgnored
          myCondition.awaitNanos(remaining);
          remaining = unit.toNanos(timeout) - (System.nanoTime() - startTime);
        }
        return myExitCode != null;
      } finally {
        myLock.unlock();
      }
    }
  }
}
