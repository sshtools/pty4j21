package com.pty4j.windows.winpty;

import static com.pty4j.Native.err;
import static com.pty4j.Native.ok;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

import org.jetbrains.annotations.Nullable;

import com.pty4j.Native;
import com.pty4j.WinSize;
import com.pty4j.unix.LibC;
import com.pty4j.util.PtyUtil;
import com.pty4j.windows.Kernel32;

/**
 * @author traff
 */
public class WinPty {

  private static final Logger LOG = System.getLogger(WinPty.class.getName());

  private static final boolean DEFAULT_MIN_INITIAL_TERMINAL_WINDOW_HEIGHT =
    !Boolean.getBoolean("disable.minimal.initial.terminal.window.height");

  private MemorySegment myWinpty;

  private MemorySegment myProcess;
  private NamedPipe myConinPipe;
  private NamedPipe myConoutPipe;
  private NamedPipe myConerrPipe;

  private boolean myChildExited = false;
  private int myStatus = -1;
  private boolean myClosed = false;
  private WinSize myLastWinSize;

  private int openInputStreamCount = 0;

  WinPty(/* @NotNull */ String cmdline,
         @Nullable String cwd,
         /* @NotNull */ String env,
         boolean consoleMode,
         @Nullable Integer initialColumns,
         @Nullable Integer initialRows,
         boolean enableAnsiColor) throws WinPtyException, IOException {
    int cols = initialColumns != null ? initialColumns : Integer.getInteger("win.pty.cols", 80);
    int rows = getInitialRows(initialRows);
    
    try(Arena mem = Arena.ofConfined()) {

        MemorySegment errCode = mem.allocate(JAVA_INT);
        MemorySegment errPtr = mem.allocate(Native.C_POINTER);
	    MemorySegment agentCfg = null;
	    MemorySegment spawnCfg = null;
	    MemorySegment winpty = null;
	    MemorySegment processHandle = mem.allocate(Native.C_POINTER);
	    NamedPipe coninPipe = null;
	    NamedPipe conoutPipe = null;
	    NamedPipe conerrPipe = null;
	
	    try {
	      // Configure the winpty agent.
	      long agentFlags = 0;
	      if (consoleMode) {
	        agentFlags = WinPtyLib.WINPTY_FLAG_CONERR | WinPtyLib.WINPTY_FLAG_PLAIN_OUTPUT;
	        if (enableAnsiColor) {
	          agentFlags |= WinPtyLib.WINPTY_FLAG_COLOR_ESCAPES;
	        }
	      }
	      agentCfg = WinPtyLib.winpty_config_new(agentFlags, MemorySegment.NULL);
	      if (agentCfg.equals(MemorySegment.NULL)) {
	        throw new WinPtyException("winpty agent cfg is null");
	      }
	      WinPtyLib.winpty_config_set_initial_size(agentCfg, cols, rows);
	      myLastWinSize = new WinSize(cols, rows);
	
	      // Start the agent.
	      winpty = WinPtyLib.winpty_open(agentCfg, errPtr);
	      if (winpty.equals(MemorySegment.NULL)) {
	        MemorySegment errMsg = WinPtyLib.winpty_error_msg(errPtr.get(Native.C_POINTER, 0));
	        String errorMessage = Native.toJavaString(errMsg, 1024);
	        if ("ConnectNamedPipe failed: Windows error 232".equals(errorMessage)) {
	          errorMessage += "\n" + suggestFixForError232();
	        }
	        throw new WinPtyException("Error starting winpty: " + errorMessage);
	      }
	
	      // Connect the pipes.  These calls return immediately (i.e. they don't block).
	      coninPipe = NamedPipe.connectToServer(Native.toJavaString(WinPtyLib.winpty_conin_name(winpty), 1024), Kernel32.GENERIC_WRITE);

	      conoutPipe = NamedPipe.connectToServer(Native.toJavaString(WinPtyLib.winpty_conout_name(winpty), 1024), Kernel32.GENERIC_READ);
	      if (consoleMode) {
	        conerrPipe = NamedPipe.connectToServer(Native.toJavaString(WinPtyLib.winpty_conerr_name(winpty), 1024), Kernel32.GENERIC_READ);
	      }
	
	      for (int i = 0; i < 5; i++) {
	        boolean result = ok(WinPtyLib.winpty_set_size(winpty, cols, rows, MemorySegment.NULL));
	        if (!result) {
	          LOG.log(Level.WARNING, "Cannot resize to workaround extra newlines issue");
	          break;
	        }
	        try {
	          Thread.sleep(10);
	        }
	        catch (InterruptedException e) {
	          e.printStackTrace();
	        }
	      }
	
	      // Spawn a child process.
	      spawnCfg = WinPtyLib.winpty_spawn_config_new(
	          WinPtyLib.WINPTY_SPAWN_FLAG_AUTO_SHUTDOWN |
	              WinPtyLib.WINPTY_SPAWN_FLAG_EXIT_AFTER_SHUTDOWN,
	          MemorySegment.NULL,
	          Native.toWideString(cmdline, mem),
	          Native.toWideString(cwd, mem),
	          Native.toWideString(env, mem),
	          MemorySegment.NULL);
	      if (spawnCfg.equals(MemorySegment.NULL)) {
	        throw new WinPtyException("winpty spawn cfg is null");
	      }
	      if (err(WinPtyLib.winpty_spawn(winpty, spawnCfg, processHandle, MemorySegment.NULL, errCode, errPtr))) {
	        MemorySegment errMsg = WinPtyLib.winpty_error_msg(errPtr.get(Native.C_POINTER, 0));
	        throw new WinPtyException("Error running process: " + Native.toJavaString(errMsg, 1024) + ". Code " + errPtr.get(Native.C_POINTER, 0));
	      }
	
	      // Success!  Save the values we want and let the `finally` block clean up the rest.
	
	      myWinpty = winpty;
	      myProcess = processHandle.get(Native.C_POINTER, 0);
	      myConinPipe = coninPipe;
	      myConoutPipe = conoutPipe;
	      myConerrPipe = conerrPipe;
	      openInputStreamCount = consoleMode ? 2 : 1;
	
	      // Designate a thread to wait for the process to exit.
	      Thread waitForExit = new WaitForExitThread();
	      waitForExit.setDaemon(true);
	      waitForExit.start();
	
	      winpty = null;
	      processHandle.set(Native.C_POINTER, 0, MemorySegment.NULL);
	      coninPipe = conoutPipe = conerrPipe = null;
	
	    } finally {
	      WinPtyLib.winpty_error_free(errPtr.get(Native.C_POINTER, 0));
	      if(agentCfg != null)
	    	  WinPtyLib.winpty_config_free(agentCfg);
	      if(spawnCfg != null)
	    	  WinPtyLib.winpty_spawn_config_free(spawnCfg);
	      if(winpty != null) {
	    	  WinPtyLib.winpty_free(winpty);
	      }
	      if (!processHandle.get(Native.C_POINTER,0).equals(MemorySegment.NULL)) {
	        Kernel32.CloseHandle(processHandle.get(Native.C_POINTER,0));
	      }
	      closeNamedPipeQuietly(coninPipe);
	      closeNamedPipeQuietly(conoutPipe);
	      closeNamedPipeQuietly(conerrPipe);
	    }
    }
  }

  /* @NotNull */
  private static String suggestFixForError232() {
    try {
      File dllFile = getLibraryPath();
      File exeFile = new File(dllFile.getParentFile(), "winpty-agent.exe");
      return "This error can occur due to antivirus blocking winpty from creating a pty. Please exclude the following files in your antivirus:\n" +
             " - " + exeFile.getAbsolutePath() + "\n" +
             " - " + dllFile.getAbsolutePath();
    }
    catch (Exception e) {
      return e.getMessage();
    }
  }

  private int getInitialRows(@Nullable Integer initialRows) {
    if (initialRows != null) {
      return initialRows;
    }
    Integer rows = Integer.getInteger("win.pty.rows");
    if (rows != null) {
      return rows;
    }
	  // workaround for https://github.com/Microsoft/console/issues/270
	  return DEFAULT_MIN_INITIAL_TERMINAL_WINDOW_HEIGHT ? 1 : 25;
  }

  private static void closeNamedPipeQuietly(NamedPipe pipe) {
    try {
      if (pipe != null) {
        pipe.close();
      }
    } catch (IOException e) {
    }
  }

  synchronized void setWinSize(/* @NotNull */ WinSize winSize) throws IOException {
    if (myClosed) {
      throw new IOException("Unable to set window size: closed=" + myClosed + ", winSize=" + winSize);
    }
    boolean result = ok(WinPtyLib.winpty_set_size(myWinpty, winSize.getColumns(), winSize.getRows(), MemorySegment.NULL));
    if (result) {
      myLastWinSize = new WinSize(winSize.getColumns(), winSize.getRows());
    }
  }

  synchronized /* @NotNull */ WinSize getWinSize() throws IOException {
    // The implementation might be improved after https://github.com/rprichard/winpty/issues/153
    WinSize lastWinSize = myLastWinSize;
    if (myClosed || lastWinSize == null) {
      throw new IOException("Unable to get window size: closed=" + myClosed + ", lastWinSize=" + lastWinSize);
    }
    return new WinSize(lastWinSize.getColumns(), lastWinSize.getRows());
  }

  synchronized void decrementOpenInputStreamCount() {
    openInputStreamCount--;
    if (openInputStreamCount == 0) {
      close();
    }
  }

  // Close the winpty_t object, which disconnects libwinpty from the winpty
  // agent process.  The agent will then close the hidden console, killing
  // everything attached to it.
  synchronized void close() {
    // This function can be called from WinPty.finalize, so its member fields
    // may have already been finalized.  The JNA Pointer class has no finalizer,
    // so it's safe to use, and the various JNA Library objects are static, so
    // they won't ever be collected.
    if (myClosed) {
      return;
    }
    WinPtyLib.winpty_free(myWinpty);
    myWinpty = null;
    myClosed = true;
    closeUnusedProcessHandle();
  }

  private synchronized void closeUnusedProcessHandle() {
    // Keep the process handle open until both conditions are met:
    //  1. The process has exited.
    //  2. We have disconnected from the agent, by closing the winpty_t
    //     object.
    // As long as the process handle is open, Windows will not reuse the child
    // process' PID.
    // https://blogs.msdn.microsoft.com/oldnewthing/20110107-00/?p=11803
    if (myClosed && myChildExited && myProcess != null) {
      Kernel32.CloseHandle(myProcess);
      myProcess = null;
    }
  }

  // Returns true if the child process is still running.  The winpty_t and
  // WinPty objects may be closed/freed either before or after the child
  // process exits.
  synchronized boolean isRunning() {
    return !myChildExited;
  }

  // Waits for the child process to exit.
  synchronized int waitFor() throws InterruptedException {
    while (!myChildExited) {
      wait();
    }
    return myStatus;
  }

  synchronized int getChildProcessId() {
    if (myClosed) {
      return -1;
    }
    return Kernel32.GetProcessId(myProcess);
  }

  synchronized int exitValue() {
    if (!myChildExited) {
      throw new IllegalThreadStateException("Process not Terminated");
    }
    return myStatus;
  }

  @Override
  protected void finalize() throws Throwable {
    close();
    super.finalize();
  }

  NamedPipe getInputPipe() {
    return myConoutPipe;
  }

  NamedPipe getOutputPipe() {
    return myConinPipe;
  }

  NamedPipe getErrorPipe() {
    return myConerrPipe;
  }

  @Nullable
  String getWorkingDirectory() throws IOException {
    if (myClosed) {
      return null;
    }
    int bufferLength = 1024;
    try(var mem = Arena.ofConfined()) {
      MemorySegment buffer = mem.allocate(2 * bufferLength);
      MemorySegment errPtr = mem.allocate(Native.C_POINTER);
      try {
	      int result = WinPtyLib.winpty_get_current_directory(myWinpty, bufferLength, buffer, errPtr);
	      if (result > 0) {
	        return Native.toJavaString(buffer, 1024);
	      }
	      MemorySegment message = WinPtyLib.winpty_error_msg(errPtr.get(Native.C_POINTER, 0));
	      int code = WinPtyLib.winpty_error_code(errPtr.get(Native.C_POINTER, 0));
	      throw new IOException("winpty_get_current_directory failed, code: " + code + ", message: " + Native.toJavaString(message, 1024));
      }
      finally {
        WinPtyLib.winpty_error_free(errPtr.get(Native.C_POINTER, 0));
      }
    }
  }

  int getConsoleProcessList() throws IOException {
    if (myClosed) {
      return 0;
    }
    int MAX_COUNT = 64;
    try(Arena mem = Arena.ofConfined()) {
        MemorySegment buffer = mem.allocate(JAVA_INT.byteSize() * MAX_COUNT);
        MemorySegment errPtr = mem.allocate(Native.C_POINTER);
	    try {
	      int actualProcessCount = WinPtyLib.winpty_get_console_process_list(myWinpty, buffer, MAX_COUNT, errPtr);
	      if (actualProcessCount == 0) {
	        MemorySegment message = WinPtyLib.winpty_error_msg(errPtr.get(Native.C_POINTER, 0));
	        int code = WinPtyLib.winpty_error_code(errPtr.get(Native.C_POINTER, 0));
	        throw new IOException("winpty_get_console_process_list failed, code: " + code + ", message: " + Native.toJavaString(message, 1024));
	      }
	      // use buffer.getIntArray(0, actualProcessCount); to get actual PIDs
	      return actualProcessCount;
	    }
	    finally {
	      WinPtyLib.winpty_error_free(errPtr.get(Native.C_POINTER, 0));
	    }
    }
  }

  // It is mostly possible to avoid using this thread; instead, the above
  // methods could call WaitForSingleObject themselves, using either a 0 or
  // INFINITE timeout as appropriate.  It is tricky, though, because we need
  // to avoid closing the process handle as long as any threads are waiting on
  // it, but we can't do an INFINITE wait inside a synchronized method.  It
  // could be done using an extra reference count, or by using DuplicateHandle
  // for INFINITE waits.
  private class WaitForExitThread extends Thread {

    @Override
    public void run() {
      Kernel32.WaitForSingleObject(myProcess, Kernel32.INFINITE);
      try(var ar = Arena.ofConfined()) {
    	  var myStatusByRef = ar.allocate(ValueLayout.JAVA_INT);
	      Kernel32.GetExitCodeProcess(myProcess, myStatusByRef);
	      synchronized (WinPty.this) {
	        WinPty.this.myChildExited = true;
	        WinPty.this.myStatus = myStatusByRef.get(ValueLayout.JAVA_INT, 0);
	        closeUnusedProcessHandle();
	        WinPty.this.notifyAll();
	      }
      }
    }
  }

  private static File getLibraryPath() {
    try {
      return PtyUtil.resolveNativeFile("winpty.dll");
    }
    catch (Exception e) {
      throw new IllegalStateException("Couldn't detect jar containing folder", e);
    }
  }

  private final static class WinPtyLib {

	public final static class Helper {

		private static final SymbolLookup SYMBOL_LOOKUP;

		static {
			SymbolLookup loaderLookup = SymbolLookup.libraryLookup(getLibraryPath().toPath(), Arena.global());
			SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LibC.LINKER.defaultLookup().find(name));
		}

		static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
			return SYMBOL_LOOKUP.find(name).map(addr -> LibC.LINKER.downcallHandle(addr, fdesc)).orElse(null);
		}
	}
	  
    /*
     * winpty API.
     */

    final static long WINPTY_FLAG_CONERR = 1;
    final static long WINPTY_FLAG_PLAIN_OUTPUT = 2;
    final static long WINPTY_FLAG_COLOR_ESCAPES = 4;

    final static long WINPTY_SPAWN_FLAG_AUTO_SHUTDOWN = 1;
    final static long WINPTY_SPAWN_FLAG_EXIT_AFTER_SHUTDOWN = 2;

    private static class winpty_error_code {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            JAVA_INT,
            Native.C_POINTER
        );
        
        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_error_code", DESC);
    }
    
    public static int winpty_error_code(MemorySegment err) {
        try {
            return (int)winpty_error_code.HANDLE.invokeExact(err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_error_msg {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_error_msg", DESC);
    }
    
    public static MemorySegment winpty_error_msg(MemorySegment err) {
        try {
            return (MemorySegment)winpty_error_msg.HANDLE.invokeExact(err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class winpty_error_free {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_error_free", DESC);
    }
    
    public static void winpty_error_free(MemorySegment err) {
        try {
        	winpty_error_free.HANDLE.invokeExact(err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_config_new {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            JAVA_LONG,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_config_new", DESC);
    }

    public static MemorySegment winpty_config_new(long agentFlags, MemorySegment err) {
        try {
            return (MemorySegment)winpty_config_new.HANDLE.invokeExact(agentFlags, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_config_free {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_config_free", DESC);
    }
    
    public static void winpty_config_free(MemorySegment cfg) {
        try {
        	winpty_config_free.HANDLE.invokeExact(cfg);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_config_set_initial_size {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            Native.C_POINTER,
            JAVA_INT,
            JAVA_INT
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_config_set_initial_size", DESC);
    }
    
    public static void winpty_config_set_initial_size(MemorySegment cfg, int cols, int rows) {
        try {
        	winpty_config_set_initial_size.HANDLE.invokeExact(cfg, cols, rows);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class winpty_open {
    	public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_open", DESC);
    }
    
    public static MemorySegment winpty_open(MemorySegment cfg, MemorySegment err) {
        try {
            return (MemorySegment)winpty_open.HANDLE.invokeExact(cfg, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_conin_name {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_conin_name", DESC);
    }

    public static MemorySegment winpty_conin_name(MemorySegment wp) {
        try {
            return (MemorySegment)winpty_conin_name.HANDLE.invokeExact(wp);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_conout_name {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_conout_name", DESC);
    }
    
    public static MemorySegment winpty_conout_name(MemorySegment wp) {
        try {
            return (MemorySegment)winpty_conout_name.HANDLE.invokeExact(wp);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_conerr_name {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_conerr_name", DESC);
    } 
    
    public static MemorySegment winpty_conerr_name(MemorySegment wp) {
        try {
            return (MemorySegment)winpty_conerr_name.HANDLE.invokeExact(wp);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    

    private static class winpty_spawn_config_new {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            Native.C_POINTER,
            JAVA_LONG,
            Native.C_POINTER,
            Native.C_POINTER,
            Native.C_POINTER,
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_spawn_config_new", DESC);
    }

    public static MemorySegment winpty_spawn_config_new(long spawnFlags, MemorySegment appname, MemorySegment cmdline, MemorySegment cwd, MemorySegment env, MemorySegment err) {
        try {
            return (MemorySegment)winpty_spawn_config_new.HANDLE.invokeExact(spawnFlags, appname, cmdline, cwd, env, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class winpty_spawn_config_free {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_spawn_config_free", DESC);
    }
    
    public static void winpty_spawn_config_free(MemorySegment cfg) {
        try {
        	winpty_spawn_config_free.HANDLE.invokeExact(cfg);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    } 
    
    private static class winpty_spawn {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
                JAVA_INT,
                Native.C_POINTER,
                Native.C_POINTER,
                Native.C_POINTER,
                Native.C_POINTER,
                Native.C_POINTER,
                Native.C_POINTER
            );


        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_spawn", DESC);
    }

    public static int winpty_spawn(MemorySegment wp, MemorySegment cfg, MemorySegment process_handle, MemorySegment thread_handle, MemorySegment create_process_error, MemorySegment err) {
        try {
            return (int)winpty_spawn.HANDLE.invokeExact(wp, cfg, process_handle, thread_handle, create_process_error, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class winpty_set_size {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
                JAVA_INT,
                Native.C_POINTER,
                JAVA_INT,
                JAVA_INT,
                Native.C_POINTER
            );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_set_size", DESC);
    }

    public static int winpty_set_size(MemorySegment wp, int cols, int rows, MemorySegment err) {
        try {
            return (int)winpty_set_size.HANDLE.invokeExact(wp, cols, rows, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class winpty_get_console_process_list {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            JAVA_INT,
            Native.C_POINTER,
            Native.C_POINTER,
            JAVA_INT,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_get_console_process_list", DESC);
    }
    
    public static int winpty_get_console_process_list(MemorySegment wp, MemorySegment processList, int processCount, MemorySegment err) {
        try {
            return (int)winpty_get_console_process_list.HANDLE.invokeExact(wp, processList, processCount, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_get_current_directory {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            JAVA_INT,
            Native.C_POINTER,
            JAVA_INT,
            Native.C_POINTER,
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_get_current_directory", DESC);
    }

    public static int winpty_get_current_directory(MemorySegment wp, int nBufferLength, MemorySegment lpBuffer, MemorySegment err) {
        try {
            return (int)winpty_get_current_directory.HANDLE.invokeExact(wp, nBufferLength, lpBuffer, err);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class winpty_free {
        public static final FunctionDescriptor DESC = FunctionDescriptor.ofVoid(
            Native.C_POINTER
        );

        public static final MethodHandle HANDLE = Helper.downcallHandle("winpty_free", DESC);
    }
    
    public static void winpty_free(MemorySegment wp) {
        try {
        	winpty_free.HANDLE.invokeExact(wp);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
  }
}
