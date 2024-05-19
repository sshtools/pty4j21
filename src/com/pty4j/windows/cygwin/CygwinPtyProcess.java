package com.pty4j.windows.cygwin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.jetbrains.annotations.NotNull;

import com.pty4j.Native;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.pty4j.util.PtyUtil;
import com.pty4j.windows.Kernel32;
import com.pty4j.windows.Kernel32.OVERLAPPED;
import com.pty4j.windows.winpty.NamedPipe;

public class CygwinPtyProcess extends PtyProcess {
  private static final int CONNECT_PIPE_TIMEOUT = 1000;

  private static final int PIPE_ACCESS_INBOUND = 1;
  private static final int PIPE_ACCESS_OUTBOUND = 2;

  private static final AtomicInteger processCounter = new AtomicInteger();

  private final Process myProcess;
  private final NamedPipe myInputPipe;
  private final NamedPipe myOutputPipe;
  private final NamedPipe myErrorPipe;
  private final MemorySegment myInputHandle;
  private final MemorySegment myOutputHandle;
  private final MemorySegment myErrorHandle;
  private final boolean myConsoleMode;

  public CygwinPtyProcess(String[] command, Map<String, String> environment, String workingDirectory, File logFile, boolean console)
    throws IOException {
    myConsoleMode = console;
    int procNo = processCounter.getAndIncrement(); 
    String pipePrefix = String.format("\\\\.\\pipe\\cygwinpty-%d-%d-", Kernel32.GetCurrentProcessId(), procNo);
    String inPipeName = pipePrefix + "in";
    String outPipeName = pipePrefix + "out";
    String errPipeName = pipePrefix + "err";

    try(Arena mem = Arena.ofConfined()) {
	    myInputHandle = Kernel32.CreateNamedPipeA(mem.allocateFrom(inPipeName), PIPE_ACCESS_OUTBOUND | Kernel32.FILE_FLAG_OVERLAPPED, 0, 1, 0, 0, 0, MemorySegment.NULL);
	    myOutputHandle = Kernel32.CreateNamedPipeA(mem.allocateFrom(outPipeName), PIPE_ACCESS_INBOUND | Kernel32.FILE_FLAG_OVERLAPPED, 0, 1, 0, 0, 0, MemorySegment.NULL);
	    myErrorHandle =
	      console ? Kernel32.CreateNamedPipeA(mem.allocateFrom(errPipeName), PIPE_ACCESS_INBOUND | Kernel32.FILE_FLAG_OVERLAPPED, 0, 1, 0, 0, 0, MemorySegment.NULL) : null;
	
	    if (myInputHandle.equals(Kernel32.INVALID_HANDLE_VALUE) ||
	        myOutputHandle.equals(Kernel32.INVALID_HANDLE_VALUE) ||
	        (console && myErrorHandle.equals(Kernel32.INVALID_HANDLE_VALUE))) {
	      closeHandles();
	      throw new IOException("Unable to create a named pipe");
	    }
    }

    myInputPipe = new NamedPipe(myInputHandle, false);
    myOutputPipe = new NamedPipe(myOutputHandle, false);
    myErrorPipe = myErrorHandle != null ? new NamedPipe(myErrorHandle, false) : null;

    myProcess = startProcess(inPipeName, outPipeName, errPipeName, workingDirectory, command, environment, logFile, console);
  }

  @Override
  public boolean isConsoleMode() {
    return myConsoleMode;
  }

  private Process startProcess(String inPipeName,
                               String outPipeName,
                               String errPipeName,
                               String workingDirectory,
                               String[] command,
                               Map<String, String> environment,
                               File logFile,
                               boolean console) throws IOException {
    File nativeFile;
    try {
      nativeFile = PtyUtil.resolveNativeFile("cyglaunch.exe");
    } catch (Exception e) {
      throw new IOException(e);
    }
    String logPath = logFile == null ? "null" : logFile.getAbsolutePath();
    ProcessBuilder processBuilder =
      new ProcessBuilder(nativeFile.getAbsolutePath(), logPath, console ? "1" : "0", inPipeName, outPipeName, errPipeName);
    for (String s : command) {
      processBuilder.command().add(s);
    }
    if (workingDirectory != null) {
      processBuilder.directory(new File(workingDirectory));
    }
    processBuilder.environment().clear();
    processBuilder.environment().putAll(environment);
    final Process process = processBuilder.start();

    try {
      waitForPipe(myInputHandle);
      waitForPipe(myOutputHandle);
      if (myErrorHandle != null) waitForPipe(myErrorHandle);
    } catch (IOException e) {
      process.destroy();
      closeHandles();
      throw e;
    }

    new Thread() {
      @Override
      public void run() {
        while (true) {
          try {
            process.waitFor();
            break;
          }
          catch (InterruptedException ignore) { }
        }

        closePipes();
      }
    }.start();

    return process;
  }

  private static void waitForPipe(MemorySegment handle) throws IOException {
    MemorySegment connectEvent = Kernel32.CreateEventA(MemorySegment.NULL, Native.TRUE, Native.FALSE, MemorySegment.NULL);

    try(Arena mem = Arena.ofConfined()) {
    	MemorySegment povl = mem.allocate(OVERLAPPED.layout());
    	OVERLAPPED.hEvent(povl, connectEvent);
	
	    boolean success = Kernel32.ConnectNamedPipe(handle, povl) != 0;
	    if (!success) {
	      switch (Kernel32.GetLastError()) {
	        case Kernel32.ERROR_PIPE_CONNECTED:
	          success = true;
	          break;
	        case Kernel32.ERROR_IO_PENDING:
	          if (Kernel32.WaitForSingleObject(connectEvent, CONNECT_PIPE_TIMEOUT) != Kernel32.WAIT_OBJECT_0) {
	            Kernel32.CancelIo(handle);
	
	            success = false;
	          }
	          else {
	            success = true;
	          }
	          break;
	      }
	    }
	
	    Kernel32.CloseHandle(connectEvent);
	
	    if (!success) throw new IOException("Cannot connect to a named pipe");
    }
  }

  @Override
  public void setWinSize(@NotNull WinSize winSize) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public @NotNull WinSize getWinSize() throws IOException {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public long pid() {
    // TODO: Dont know yet how to get pid. Need to test on Windows.
    return -1;
  }

  @Override
  public OutputStream getOutputStream() {
    return new CygwinPTYOutputStream(myInputPipe);
  }

  @Override
  public InputStream getInputStream() {
    return new CygwinPTYInputStream(myOutputPipe);
  }

  @Override
  public InputStream getErrorStream() {
    if (myErrorPipe == null) {
      return new InputStream() {
        @Override
        public int read() throws IOException {
          return -1;
        }
      };
    }
    return new CygwinPTYInputStream(myErrorPipe);
  }

  @Override
  public int waitFor() throws InterruptedException {
    return myProcess.waitFor();
  }

  @Override
  public int exitValue() {
    return myProcess.exitValue();
  }

  @Override
  public void destroy() {
    myProcess.destroy();
  }

  @Override
  public byte getEnterKeyCode() {
    // Created pty ignores carriage return: output of `stty --file /dev/pty1 -a` contains "igncr".
    // See https://man7.org/linux/man-pages/man1/stty.1.html for details.
    // Other relevant input options of the created pty: "-inlcr igncr icrnl".
    // This means CR is translated to LF anyway. Let's send LF.
    return '\n';
  }

  private void closeHandles() {
    Kernel32.CloseHandle(myInputHandle);
    Kernel32.CloseHandle(myOutputHandle);
    if (myErrorHandle != null) Kernel32.CloseHandle(myErrorHandle);
  }

  private void closePipes() {
    myInputPipe.markClosed();
    myOutputPipe.markClosed();
    if (myErrorPipe != null) myErrorPipe.markClosed();
  }
}
