package testData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.nio.charset.StandardCharsets;

import org.jetbrains.annotations.NotNull;

import com.pty4j.Native;
import com.pty4j.Platform;
import com.pty4j.TestUtil;
import com.pty4j.WinSize;
import com.pty4j.unix.PtyHelpers;
import com.pty4j.windows.Kernel32;
import com.pty4j.windows.conpty.ConPtyLibrary;
import com.pty4j.windows.conpty.ConPtyLibrary._SMALL_RECT;

public class ConsoleSizeReporter {

  public static final String PRINT_SIZE = "print_size";
  public static final String EXIT = "exit";

  public static void main(String[] args) throws IOException {
    TestUtil.assertConsoleExists();
    TestUtil.setLocalPtyLib();
    printSize();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    String line;
    while ((line = reader.readLine()) != null) {
      if (line.equals(PRINT_SIZE)) {
        printSize();
      }
      else if (line.equals(EXIT)) {
        break;
      }
    }
  }

  private static void printSize() throws IOException {
    WinSize windowSize = getWindowSize();
    System.out.println("columns: " + windowSize.getColumns() + ", rows: " + windowSize.getRows());
  }

  private static @NotNull WinSize getWindowSize() throws IOException {
    if (Platform.isWindows()) {
      MemorySegment handle = Kernel32.GetStdHandle(Kernel32.STD_OUTPUT_HANDLE);
      try(Arena mem = Arena.ofConfined()) {
	      MemorySegment buffer = mem.allocate(ConPtyLibrary._CONSOLE_SCREEN_BUFFER_INFO.layout()); 
	      if (Native.err(Kernel32.GetConsoleScreenBufferInfo(handle, buffer))) {
	        throw new IOException("GetConsoleScreenBufferInfo failed");
	      }
	      MemorySegment window = ConPtyLibrary._CONSOLE_SCREEN_BUFFER_INFO.srWindow(buffer);
	      return new WinSize(_SMALL_RECT.Right(window) - ConPtyLibrary._SMALL_RECT.Left(window) + 1, _SMALL_RECT.Bottom(window) - _SMALL_RECT.Top(window) + 1);
      }
    }
    else {
      return PtyHelpers.getWinSize(0, null);
    }
  }
}
