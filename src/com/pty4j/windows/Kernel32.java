package com.pty4j.windows;

import static com.pty4j.Native.C_POINTER;
import static java.lang.foreign.MemoryLayout.PathElement.groupElement;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;

import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout.OfInt;
import java.lang.foreign.ValueLayout.OfLong;
import java.lang.foreign.ValueLayout.OfShort;
import java.lang.invoke.MethodHandle;
import java.util.function.Consumer;

import com.pty4j.Native;
import com.pty4j.unix.LibC;

public final class Kernel32 {
	
	public static final int MAX_PATH = 260;

	public static final int ERROR_PIPE_CONNECTED = (int) 535L;
	public static final int ERROR_IO_PENDING = (int) 997L;
	public static final int ERROR_BROKEN_PIPE = (int) 109L;

	public static final int OPEN_EXISTING = (int) 3L;

	public static final int WAIT_OBJECT_0 = (int) 0L;
	public static final int WAIT_FAILED = (int)4294967295L;

	public static final int GENERIC_READ = (int) 2147483648L;
	public static final int GENERIC_WRITE = (int) 1073741824L;

	public static final int STARTF_USESTDHANDLES = (int) 256L;
	public static final int EXTENDED_STARTUPINFO_PRESENT = (int) 524288L;
	public static final int CREATE_UNICODE_ENVIRONMENT = (int) 1024L;
	public static final int STD_OUTPUT_HANDLE = (int)4294967285L;

    public static final int FORMAT_MESSAGE_ALLOCATE_BUFFER = (int)256L;
    public static final int FORMAT_MESSAGE_FROM_SYSTEM = (int)4096L;
    public static final int FORMAT_MESSAGE_IGNORE_INSERTS = (int)512L;

	public static final long PROC_THREAD_ATTRIBUTE_PSEUDOCONSOLE = 0x00020016L;
	public static final int FILE_FLAG_OVERLAPPED = (int) 1073741824L;
	public static final int INFINITE = (int) 4294967295L;
	public static final MemorySegment INVALID_HANDLE_VALUE = MemorySegment.ofAddress(-1L);
	
	public static final int S_OK = 0;

	private static class CloseHandle {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CloseHandle", DESC);
	}

	public static int CloseHandle(MemorySegment hObject) {
		try {
			return (int) CloseHandle.HANDLE.invokeExact(hObject);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class FreeConsole {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("FreeConsole", DESC);
	}

	public static int FreeConsole() {
		try {
			return (int) FreeConsole.HANDLE.invokeExact();
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class AttachConsole {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("AttachConsole", DESC);
	}

	public static int AttachConsole(int dwProcessId) {
		try {
			return (int) AttachConsole.HANDLE.invokeExact(dwProcessId);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetConsoleProcessList {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetConsoleProcessList", DESC);
	}

	public static int GetConsoleProcessList(MemorySegment lpdwProcessList, int dwProcessCount) {
		try {
			return (int) GetConsoleProcessList.HANDLE.invokeExact(lpdwProcessList, dwProcessCount);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetProcessId {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetProcessId", DESC);
	}

	public static int GetProcessId(MemorySegment Process) {
		try {
			return (int) GetProcessId.HANDLE.invokeExact(Process);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CancelIo {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CancelIo", DESC);
	}

	public static int CancelIo(MemorySegment hFile) {
		try {
			return (int) CancelIo.HANDLE.invokeExact(hFile);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class WaitForMultipleObjects {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, JAVA_INT, Native.C_POINTER,
				JAVA_INT, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("WaitForMultipleObjects", DESC);
	}

	public static int WaitForMultipleObjects(int nCount, MemorySegment lpHandles, int bWaitAll, int dwMilliseconds) {
		try {
			return (int) WaitForMultipleObjects.HANDLE.invokeExact(nCount, lpHandles, bWaitAll, dwMilliseconds);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class WaitForSingleObject {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("WaitForSingleObject", DESC);
	}

	public static int WaitForSingleObject(MemorySegment hHandle, int dwMilliseconds) {
		try {
			return (int) WaitForSingleObject.HANDLE.invokeExact(hHandle, dwMilliseconds);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreateEventA {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(Native.C_POINTER, Native.C_POINTER,
				JAVA_INT, JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreateEventA", DESC);
	}

	public static MemorySegment CreateEventA(MemorySegment lpEventAttributes, int bManualReset, int bInitialState,
			MemorySegment lpName) {
		try {
			return (MemorySegment) CreateEventA.HANDLE.invokeExact(lpEventAttributes, bManualReset, bInitialState,
					lpName);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreateEventW {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(Native.C_POINTER, Native.C_POINTER,
				JAVA_INT, JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreateEventW", DESC);
	}

	public static MemorySegment CreateEvent(MemorySegment lpEventAttributes, int bManualReset, int bInitialState,
			MemorySegment lpName) {
		try {
			return (MemorySegment) CreateEventW.HANDLE.invokeExact(lpEventAttributes, bManualReset, bInitialState,
					lpName);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetLastError {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetLastError", DESC);
	}

	/**
	 * Deprecated. Java 21 has mechanism for dealing with this properly.
	 * https://stackoverflow.com/questions/73841450/obtain-the-native-value-of-errno-from-java-in-macos
	 * 
	 * @return
	 */
	@Deprecated
	public static int GetLastError() {
		try {
			return (int) GetLastError.HANDLE.invokeExact();
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetCurrentProcessId {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetCurrentProcessId", DESC);
	}

	public static int GetCurrentProcessId() {
		try {
			return (int) GetCurrentProcessId.HANDLE.invokeExact();
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class SetEvent {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("SetEvent", DESC);
	}

	public static int SetEvent(MemorySegment hEvent) {
		try {
			return (int) SetEvent.HANDLE.invokeExact(hEvent);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetExitCodeProcess {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetExitCodeProcess", DESC);
	}

	public static int GetExitCodeProcess(MemorySegment hProcess, MemorySegment lpExitCode) {
		try {
			return (int) GetExitCodeProcess.HANDLE.invokeExact(hProcess, lpExitCode);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreatePipe {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER, Native.C_POINTER, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreatePipe", DESC);
	}

	public static int CreatePipe(MemorySegment hReadPipe, MemorySegment hWritePipe, MemorySegment lpPipeAttributes,
			int nSize) {
		try {
			return (int) CreatePipe.HANDLE.invokeExact(hReadPipe, hWritePipe, lpPipeAttributes, nSize);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class TerminateProcess {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("TerminateProcess", DESC);
	}

	public static int TerminateProcess(MemorySegment hProcess, int uExitCode) {
		try {
			return (int) TerminateProcess.HANDLE.invokeExact(hProcess, uExitCode);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreateNamedPipeA {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(Native.C_POINTER, Native.C_POINTER,
				JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreateNamedPipeA", DESC);
	}

	public static MemorySegment CreateNamedPipeA(MemorySegment lpName, int dwOpenMode, int dwPipeMode,
			int nMaxInstances, int nOutBufferSize, int nInBufferSize, int nDefaultTimeOut,
			MemorySegment lpSecurityAttributes) {
		try {
			return (MemorySegment) CreateNamedPipeA.HANDLE.invokeExact(lpName, dwOpenMode, dwPipeMode, nMaxInstances,
					nOutBufferSize, nInBufferSize, nDefaultTimeOut, lpSecurityAttributes);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class ConnectNamedPipe {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("ConnectNamedPipe", DESC);
	}

	public static int ConnectNamedPipe(MemorySegment hNamedPipe, MemorySegment lpOverlapped) {
		try {
			return (int) ConnectNamedPipe.HANDLE.invokeExact(hNamedPipe, lpOverlapped);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class PeekNamedPipe {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER, JAVA_INT, Native.C_POINTER, Native.C_POINTER, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("PeekNamedPipe", DESC);
	}

	public static int PeekNamedPipe(MemorySegment hNamedPipe, MemorySegment lpBuffer, int nBufferSize,
			MemorySegment lpBytesRead, MemorySegment lpTotalBytesAvail, MemorySegment lpBytesLeftThisMessage) {
		try {
			return (int) PeekNamedPipe.HANDLE.invokeExact(hNamedPipe, lpBuffer, nBufferSize, lpBytesRead,
					lpTotalBytesAvail, lpBytesLeftThisMessage);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class ReadFile {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER, JAVA_INT, Native.C_POINTER, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("ReadFile", DESC);
	}

	public static int ReadFile(MemorySegment hFile, MemorySegment lpBuffer, int nNumberOfBytesToRead,
			MemorySegment lpNumberOfBytesRead, MemorySegment lpOverlapped) {
		try {
			return (int) ReadFile.HANDLE.invokeExact(hFile, lpBuffer, nNumberOfBytesToRead, lpNumberOfBytesRead,
					lpOverlapped);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class WriteFile {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER, JAVA_INT, Native.C_POINTER, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("WriteFile", DESC);
	}

	public static int WriteFile(MemorySegment hFile, MemorySegment lpBuffer, int nNumberOfBytesToWrite,
			MemorySegment lpNumberOfBytesWritten, MemorySegment lpOverlapped) {
		try {
			return (int) WriteFile.HANDLE.invokeExact(hFile, lpBuffer, nNumberOfBytesToWrite, lpNumberOfBytesWritten,
					lpOverlapped);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class GetOverlappedResult {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, Native.C_POINTER,
				Native.C_POINTER, Native.C_POINTER, JAVA_INT);

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetOverlappedResult", DESC);
	}

	public static int GetOverlappedResult(MemorySegment hFile, MemorySegment lpOverlapped,
			MemorySegment lpNumberOfBytesTransferred, int bWait) {
		try {
			return (int) GetOverlappedResult.HANDLE.invokeExact(hFile, lpOverlapped, lpNumberOfBytesTransferred, bWait);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class InitializeProcThreadAttributeList {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, C_POINTER, JAVA_INT, JAVA_INT,
				C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("InitializeProcThreadAttributeList", DESC);
	}

	public static int InitializeProcThreadAttributeList(MemorySegment lpAttributeList, int dwAttributeCount,
			int dwFlags, MemorySegment lpSize) {
		try {
			return (int) InitializeProcThreadAttributeList.HANDLE.invokeExact(lpAttributeList, dwAttributeCount,
					dwFlags, lpSize);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class UpdateProcThreadAttribute {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, C_POINTER, JAVA_INT, JAVA_LONG,
				C_POINTER, JAVA_LONG, C_POINTER, C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("UpdateProcThreadAttribute", DESC);
	}

	public static int UpdateProcThreadAttribute(MemorySegment lpAttributeList, int dwFlags, long Attribute,
			MemorySegment lpValue, long cbSize, MemorySegment lpPreviousValue, MemorySegment lpReturnSize) {
		try {
			return (int) UpdateProcThreadAttribute.HANDLE.invokeExact(lpAttributeList, dwFlags, Attribute, lpValue,
					cbSize, lpPreviousValue, lpReturnSize);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreateFileW {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(Native.C_POINTER, Native.C_POINTER,
				JAVA_INT, JAVA_INT, Native.C_POINTER, JAVA_INT, JAVA_INT, Native.C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreateFileW", DESC);
	}

	public static MemorySegment CreateFileW(MemorySegment lpFileName, int dwDesiredAccess, int dwShareMode,
			MemorySegment lpSecurityAttributes, int dwCreationDisposition, int dwFlagsAndAttributes,
			MemorySegment hTemplateFile) {
		try {
			return (MemorySegment) CreateFileW.HANDLE.invokeExact(lpFileName, dwDesiredAccess, dwShareMode,
					lpSecurityAttributes, dwCreationDisposition, dwFlagsAndAttributes, hTemplateFile);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	}

	private static class CreateProcessW {
		public static final FunctionDescriptor DESC = FunctionDescriptor.of(JAVA_INT, C_POINTER, C_POINTER, C_POINTER,
				C_POINTER, JAVA_INT, JAVA_INT, C_POINTER, C_POINTER, C_POINTER, C_POINTER);

		public static final MethodHandle HANDLE = Helper.downcallHandle("CreateProcessW", DESC);
	}

	public static int CreateProcessW(MemorySegment lpApplicationName, MemorySegment lpCommandLine,
			MemorySegment lpProcessAttributes, MemorySegment lpThreadAttributes, int bInheritHandles,
			int dwCreationFlags, MemorySegment lpEnvironment, MemorySegment lpCurrentDirectory,
			MemorySegment lpStartupInfo, MemorySegment lpProcessInformation) {
		try {
			return (int) CreateProcessW.HANDLE.invokeExact(lpApplicationName, lpCommandLine, lpProcessAttributes,
					lpThreadAttributes, bInheritHandles, dwCreationFlags, lpEnvironment, lpCurrentDirectory,
					lpStartupInfo, lpProcessInformation);
		} catch (Throwable ex$) {
			throw new AssertionError("should not reach here", ex$);
		}
	} 
	
	private static class FormatMessageW {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
                JAVA_INT,
                JAVA_INT,
                C_POINTER,
                JAVA_INT,
                JAVA_INT,
                C_POINTER,
                JAVA_INT,
                C_POINTER
            );

		public static final MethodHandle HANDLE = Helper.downcallHandle("FormatMessageW", DESC);
    }
	
	public static int FormatMessageW(int dwFlags, MemorySegment lpSource, int dwMessageId, int dwLanguageId, MemorySegment lpBuffer, int nSize, MemorySegment Arguments) {
        try {
            MethodHandle handle = FormatMessageW.HANDLE;
			return (int)handle.invokeExact(dwFlags, lpSource, dwMessageId, dwLanguageId, lpBuffer, nSize, Arguments);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

    private static class GetStdHandle {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
            C_POINTER,
            JAVA_INT
        );

		public static final MethodHandle HANDLE = Helper.downcallHandle("GetStdHandle", DESC);
    }
    
    public static MemorySegment GetStdHandle(int nStdHandle) {
        try {
            return (MemorySegment)GetStdHandle.HANDLE.invokeExact(nStdHandle);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }
    
    private static class GetConsoleScreenBufferInfo {
        public static final FunctionDescriptor DESC = FunctionDescriptor.of(
                JAVA_INT,
                C_POINTER,
                C_POINTER
            );
		public static final MethodHandle HANDLE = Helper.downcallHandle("GetConsoleScreenBufferInfo", DESC);
    }
    
    public static int GetConsoleScreenBufferInfo(MemorySegment hConsoleOutput, MemorySegment lpConsoleScreenBufferInfo) {
        try {
            return (int)GetConsoleScreenBufferInfo.HANDLE.invokeExact(hConsoleOutput, lpConsoleScreenBufferInfo);
        } catch (Throwable ex$) {
           throw new AssertionError("should not reach here", ex$);
        }
    }

	public final static class Helper {

		private static final SymbolLookup SYMBOL_LOOKUP;

		static {
			SymbolLookup loaderLookup = Native.load("kernel32", Arena.global());
			SYMBOL_LOOKUP = name -> loaderLookup.find(name).or(() -> LibC.LINKER.defaultLookup().find(name));
		}

		static MethodHandle downcallHandle(String name, FunctionDescriptor fdesc) {
			return SYMBOL_LOOKUP.find(name).map(addr -> LibC.LINKER.downcallHandle(addr, fdesc)).orElseThrow(() -> new IllegalArgumentException("No " + name));
		}
	}

	/**
	 * {@snippet lang = c :
	 * struct _PROCESS_INFORMATION {
	 *     HANDLE hProcess;
	 *     HANDLE hThread;
	 *     DWORD dwProcessId;
	 *     DWORD dwThreadId;
	 * }
	 * }
	 */
	public final static class PROCESS_INFORMATION {

		PROCESS_INFORMATION() {
			// Should not be called directly
		}

		private static final GroupLayout $LAYOUT = MemoryLayout
				.structLayout(C_POINTER.withName("hProcess"), C_POINTER.withName("hThread"),
						JAVA_INT.withName("dwProcessId"), JAVA_INT.withName("dwThreadId"))
				.withName("_PROCESS_INFORMATION");

		/**
		 * The layout of this struct
		 */
		public static final GroupLayout layout() {
			return $LAYOUT;
		}

		private static final AddressLayout hProcess$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("hProcess"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hProcess
		 * }
		 */
		public static final AddressLayout hProcess$layout() {
			return hProcess$LAYOUT;
		}

		private static final long hProcess$OFFSET = 0;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hProcess
		 * }
		 */
		public static final long hProcess$offset() {
			return hProcess$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hProcess
		 * }
		 */
		public static MemorySegment hProcess(MemorySegment struct) {
			return struct.get(hProcess$LAYOUT, hProcess$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hProcess
		 * }
		 */
		public static void hProcess(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hProcess$LAYOUT, hProcess$OFFSET, fieldValue);
		}

		private static final AddressLayout hThread$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("hThread"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hThread
		 * }
		 */
		public static final AddressLayout hThread$layout() {
			return hThread$LAYOUT;
		}

		private static final long hThread$OFFSET = 8;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hThread
		 * }
		 */
		public static final long hThread$offset() {
			return hThread$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hThread
		 * }
		 */
		public static MemorySegment hThread(MemorySegment struct) {
			return struct.get(hThread$LAYOUT, hThread$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hThread
		 * }
		 */
		public static void hThread(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hThread$LAYOUT, hThread$OFFSET, fieldValue);
		}

		private static final OfInt dwProcessId$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwProcessId"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwProcessId
		 * }
		 */
		public static final OfInt dwProcessId$layout() {
			return dwProcessId$LAYOUT;
		}

		private static final long dwProcessId$OFFSET = 16;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwProcessId
		 * }
		 */
		public static final long dwProcessId$offset() {
			return dwProcessId$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwProcessId
		 * }
		 */
		public static int dwProcessId(MemorySegment struct) {
			return struct.get(dwProcessId$LAYOUT, dwProcessId$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwProcessId
		 * }
		 */
		public static void dwProcessId(MemorySegment struct, int fieldValue) {
			struct.set(dwProcessId$LAYOUT, dwProcessId$OFFSET, fieldValue);
		}

		private static final OfInt dwThreadId$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwThreadId"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwThreadId
		 * }
		 */
		public static final OfInt dwThreadId$layout() {
			return dwThreadId$LAYOUT;
		}

		private static final long dwThreadId$OFFSET = 20;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwThreadId
		 * }
		 */
		public static final long dwThreadId$offset() {
			return dwThreadId$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwThreadId
		 * }
		 */
		public static int dwThreadId(MemorySegment struct) {
			return struct.get(dwThreadId$LAYOUT, dwThreadId$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwThreadId
		 * }
		 */
		public static void dwThreadId(MemorySegment struct, int fieldValue) {
			struct.set(dwThreadId$LAYOUT, dwThreadId$OFFSET, fieldValue);
		}

		/**
		 * Obtains a slice of {@code arrayParam} which selects the array element at
		 * {@code index}. The returned segment has address
		 * {@code arrayParam.address() + index * layout().byteSize()}
		 */
		public static MemorySegment asSlice(MemorySegment array, long index) {
			return array.asSlice(layout().byteSize() * index);
		}

		/**
		 * The size (in bytes) of this struct
		 */
		public static long sizeof() {
			return layout().byteSize();
		}

		/**
		 * Allocate a segment of size {@code layout().byteSize()} using
		 * {@code allocator}
		 */
		public static MemorySegment allocate(SegmentAllocator allocator) {
			return allocator.allocate(layout());
		}

		/**
		 * Allocate an array of size {@code elementCount} using {@code allocator}. The
		 * returned segment has size {@code elementCount * layout().byteSize()}.
		 */
		public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
			return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
			return reinterpret(addr, 1, arena, cleanup);
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * elementCount * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena,
				Consumer<MemorySegment> cleanup) {
			return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
		}
	}

	public class OVERLAPPED {

		OVERLAPPED() {
			// Should not be called directly
		}

		private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(JAVA_LONG.withName("Internal"),
				JAVA_LONG.withName("InternalHigh"),
				MemoryLayout
						.unionLayout(
								MemoryLayout.structLayout(JAVA_INT.withName("Offset"), JAVA_INT.withName("OffsetHigh"))
										.withName("$anon$56:9"),
								Native.C_POINTER.withName("Pointer"))
						.withName("$anon$55:5"),
				Native.C_POINTER.withName("hEvent")).withName("_OVERLAPPED");

		/**
		 * The layout of this struct
		 */
		public static final GroupLayout layout() {
			return $LAYOUT;
		}

		private static final OfLong Internal$LAYOUT = (OfLong) $LAYOUT.select(groupElement("Internal"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * ULONG_PTR Internal
		 * }
		 */
		public static final OfLong Internal$layout() {
			return Internal$LAYOUT;
		}

		private static final long Internal$OFFSET = 0;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * ULONG_PTR Internal
		 * }
		 */
		public static final long Internal$offset() {
			return Internal$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * ULONG_PTR Internal
		 * }
		 */
		public static long Internal(MemorySegment struct) {
			return struct.get(Internal$LAYOUT, Internal$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * ULONG_PTR Internal
		 * }
		 */
		public static void Internal(MemorySegment struct, long fieldValue) {
			struct.set(Internal$LAYOUT, Internal$OFFSET, fieldValue);
		}

		private static final OfLong InternalHigh$LAYOUT = (OfLong) $LAYOUT.select(groupElement("InternalHigh"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * ULONG_PTR InternalHigh
		 * }
		 */
		public static final OfLong InternalHigh$layout() {
			return InternalHigh$LAYOUT;
		}

		private static final long InternalHigh$OFFSET = 8;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * ULONG_PTR InternalHigh
		 * }
		 */
		public static final long InternalHigh$offset() {
			return InternalHigh$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * ULONG_PTR InternalHigh
		 * }
		 */
		public static long InternalHigh(MemorySegment struct) {
			return struct.get(InternalHigh$LAYOUT, InternalHigh$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * ULONG_PTR InternalHigh
		 * }
		 */
		public static void InternalHigh(MemorySegment struct, long fieldValue) {
			struct.set(InternalHigh$LAYOUT, InternalHigh$OFFSET, fieldValue);
		}

		private static final OfInt Offset$LAYOUT = (OfInt) $LAYOUT.select(groupElement("$anon$55:5"),
				groupElement("$anon$56:9"), groupElement("Offset"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD Offset
		 * }
		 */
		public static final OfInt Offset$layout() {
			return Offset$LAYOUT;
		}

		private static final long Offset$OFFSET = 16;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD Offset
		 * }
		 */
		public static final long Offset$offset() {
			return Offset$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD Offset
		 * }
		 */
		public static int Offset(MemorySegment struct) {
			return struct.get(Offset$LAYOUT, Offset$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD Offset
		 * }
		 */
		public static void Offset(MemorySegment struct, int fieldValue) {
			struct.set(Offset$LAYOUT, Offset$OFFSET, fieldValue);
		}

		private static final OfInt OffsetHigh$LAYOUT = (OfInt) $LAYOUT.select(groupElement("$anon$55:5"),
				groupElement("$anon$56:9"), groupElement("OffsetHigh"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD OffsetHigh
		 * }
		 */
		public static final OfInt OffsetHigh$layout() {
			return OffsetHigh$LAYOUT;
		}

		private static final long OffsetHigh$OFFSET = 20;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD OffsetHigh
		 * }
		 */
		public static final long OffsetHigh$offset() {
			return OffsetHigh$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD OffsetHigh
		 * }
		 */
		public static int OffsetHigh(MemorySegment struct) {
			return struct.get(OffsetHigh$LAYOUT, OffsetHigh$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD OffsetHigh
		 * }
		 */
		public static void OffsetHigh(MemorySegment struct, int fieldValue) {
			struct.set(OffsetHigh$LAYOUT, OffsetHigh$OFFSET, fieldValue);
		}

		private static final AddressLayout Pointer$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("$anon$55:5"),
				groupElement("Pointer"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * PVOID Pointer
		 * }
		 */
		public static final AddressLayout Pointer$layout() {
			return Pointer$LAYOUT;
		}

		private static final long Pointer$OFFSET = 16;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * PVOID Pointer
		 * }
		 */
		public static final long Pointer$offset() {
			return Pointer$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * PVOID Pointer
		 * }
		 */
		public static MemorySegment Pointer(MemorySegment struct) {
			return struct.get(Pointer$LAYOUT, Pointer$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * PVOID Pointer
		 * }
		 */
		public static void Pointer(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(Pointer$LAYOUT, Pointer$OFFSET, fieldValue);
		}

		private static final AddressLayout hEvent$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("hEvent"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hEvent
		 * }
		 */
		public static final AddressLayout hEvent$layout() {
			return hEvent$LAYOUT;
		}

		private static final long hEvent$OFFSET = 24;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hEvent
		 * }
		 */
		public static final long hEvent$offset() {
			return hEvent$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hEvent
		 * }
		 */
		public static MemorySegment hEvent(MemorySegment struct) {
			return struct.get(hEvent$LAYOUT, hEvent$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hEvent
		 * }
		 */
		public static void hEvent(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hEvent$LAYOUT, hEvent$OFFSET, fieldValue);
		}

		/**
		 * Obtains a slice of {@code arrayParam} which selects the array element at
		 * {@code index}. The returned segment has address
		 * {@code arrayParam.address() + index * layout().byteSize()}
		 */
		public static MemorySegment asSlice(MemorySegment array, long index) {
			return array.asSlice(layout().byteSize() * index);
		}

		/**
		 * The size (in bytes) of this struct
		 */
		public static long sizeof() {
			return layout().byteSize();
		}

		/**
		 * Allocate a segment of size {@code layout().byteSize()} using
		 * {@code allocator}
		 */
		public static MemorySegment allocate(SegmentAllocator allocator) {
			return allocator.allocate(layout());
		}

		/**
		 * Allocate an array of size {@code elementCount} using {@code allocator}. The
		 * returned segment has size {@code elementCount * layout().byteSize()}.
		 */
		public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
			return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
			return reinterpret(addr, 1, arena, cleanup);
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * elementCount * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena,
				Consumer<MemorySegment> cleanup) {
			return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
		}
	}

	/**
	 * {@snippet lang = c :
	 * struct _STARTUPINFOA {
	 *     DWORD cb;
	 *     LPSTR lpReserved;
	 *     LPSTR lpDesktop;
	 *     LPSTR lpTitle;
	 *     DWORD dwX;
	 *     DWORD dwY;
	 *     DWORD dwXSize;
	 *     DWORD dwYSize;
	 *     DWORD dwXCountChars;
	 *     DWORD dwYCountChars;
	 *     DWORD dwFillAttribute;
	 *     DWORD dwFlags;
	 *     WORD wShowWindow;
	 *     WORD cbReserved2;
	 *     LPBYTE lpReserved2;
	 *     HANDLE hStdInput;
	 *     HANDLE hStdOutput;
	 *     HANDLE hStdError;
	 * }
	 * }
	 */
	public final static class STARTUPINFOA {

		STARTUPINFOA() {
			// Should not be called directly
		}

		private static final GroupLayout $LAYOUT = MemoryLayout.structLayout(JAVA_INT.withName("cb"),
				MemoryLayout.paddingLayout(4), C_POINTER.withName("lpReserved"), C_POINTER.withName("lpDesktop"),
				C_POINTER.withName("lpTitle"), JAVA_INT.withName("dwX"), JAVA_INT.withName("dwY"),
				JAVA_INT.withName("dwXSize"), JAVA_INT.withName("dwYSize"), JAVA_INT.withName("dwXCountChars"),
				JAVA_INT.withName("dwYCountChars"), JAVA_INT.withName("dwFillAttribute"), JAVA_INT.withName("dwFlags"),
				JAVA_SHORT.withName("wShowWindow"), JAVA_SHORT.withName("cbReserved2"), MemoryLayout.paddingLayout(4),
				C_POINTER.withName("lpReserved2"), C_POINTER.withName("hStdInput"), C_POINTER.withName("hStdOutput"),
				C_POINTER.withName("hStdError")).withName("_STARTUPINFOA");

		/**
		 * The layout of this struct
		 */
		public static final GroupLayout layout() {
			return $LAYOUT;
		}

		private static final OfInt cb$LAYOUT = (OfInt) $LAYOUT.select(groupElement("cb"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD cb
		 * }
		 */
		public static final OfInt cb$layout() {
			return cb$LAYOUT;
		}

		private static final long cb$OFFSET = 0;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD cb
		 * }
		 */
		public static final long cb$offset() {
			return cb$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD cb
		 * }
		 */
		public static int cb(MemorySegment struct) {
			return struct.get(cb$LAYOUT, cb$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD cb
		 * }
		 */
		public static void cb(MemorySegment struct, int fieldValue) {
			struct.set(cb$LAYOUT, cb$OFFSET, fieldValue);
		}

		private static final AddressLayout lpReserved$LAYOUT = (AddressLayout) $LAYOUT
				.select(groupElement("lpReserved"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * LPSTR lpReserved
		 * }
		 */
		public static final AddressLayout lpReserved$layout() {
			return lpReserved$LAYOUT;
		}

		private static final long lpReserved$OFFSET = 8;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * LPSTR lpReserved
		 * }
		 */
		public static final long lpReserved$offset() {
			return lpReserved$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * LPSTR lpReserved
		 * }
		 */
		public static MemorySegment lpReserved(MemorySegment struct) {
			return struct.get(lpReserved$LAYOUT, lpReserved$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * LPSTR lpReserved
		 * }
		 */
		public static void lpReserved(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(lpReserved$LAYOUT, lpReserved$OFFSET, fieldValue);
		}

		private static final AddressLayout lpDesktop$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("lpDesktop"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * LPSTR lpDesktop
		 * }
		 */
		public static final AddressLayout lpDesktop$layout() {
			return lpDesktop$LAYOUT;
		}

		private static final long lpDesktop$OFFSET = 16;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * LPSTR lpDesktop
		 * }
		 */
		public static final long lpDesktop$offset() {
			return lpDesktop$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * LPSTR lpDesktop
		 * }
		 */
		public static MemorySegment lpDesktop(MemorySegment struct) {
			return struct.get(lpDesktop$LAYOUT, lpDesktop$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * LPSTR lpDesktop
		 * }
		 */
		public static void lpDesktop(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(lpDesktop$LAYOUT, lpDesktop$OFFSET, fieldValue);
		}

		private static final AddressLayout lpTitle$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("lpTitle"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * LPSTR lpTitle
		 * }
		 */
		public static final AddressLayout lpTitle$layout() {
			return lpTitle$LAYOUT;
		}

		private static final long lpTitle$OFFSET = 24;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * LPSTR lpTitle
		 * }
		 */
		public static final long lpTitle$offset() {
			return lpTitle$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * LPSTR lpTitle
		 * }
		 */
		public static MemorySegment lpTitle(MemorySegment struct) {
			return struct.get(lpTitle$LAYOUT, lpTitle$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * LPSTR lpTitle
		 * }
		 */
		public static void lpTitle(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(lpTitle$LAYOUT, lpTitle$OFFSET, fieldValue);
		}

		private static final OfInt dwX$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwX"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwX
		 * }
		 */
		public static final OfInt dwX$layout() {
			return dwX$LAYOUT;
		}

		private static final long dwX$OFFSET = 32;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwX
		 * }
		 */
		public static final long dwX$offset() {
			return dwX$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwX
		 * }
		 */
		public static int dwX(MemorySegment struct) {
			return struct.get(dwX$LAYOUT, dwX$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwX
		 * }
		 */
		public static void dwX(MemorySegment struct, int fieldValue) {
			struct.set(dwX$LAYOUT, dwX$OFFSET, fieldValue);
		}

		private static final OfInt dwY$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwY"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwY
		 * }
		 */
		public static final OfInt dwY$layout() {
			return dwY$LAYOUT;
		}

		private static final long dwY$OFFSET = 36;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwY
		 * }
		 */
		public static final long dwY$offset() {
			return dwY$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwY
		 * }
		 */
		public static int dwY(MemorySegment struct) {
			return struct.get(dwY$LAYOUT, dwY$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwY
		 * }
		 */
		public static void dwY(MemorySegment struct, int fieldValue) {
			struct.set(dwY$LAYOUT, dwY$OFFSET, fieldValue);
		}

		private static final OfInt dwXSize$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwXSize"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwXSize
		 * }
		 */
		public static final OfInt dwXSize$layout() {
			return dwXSize$LAYOUT;
		}

		private static final long dwXSize$OFFSET = 40;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwXSize
		 * }
		 */
		public static final long dwXSize$offset() {
			return dwXSize$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwXSize
		 * }
		 */
		public static int dwXSize(MemorySegment struct) {
			return struct.get(dwXSize$LAYOUT, dwXSize$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwXSize
		 * }
		 */
		public static void dwXSize(MemorySegment struct, int fieldValue) {
			struct.set(dwXSize$LAYOUT, dwXSize$OFFSET, fieldValue);
		}

		private static final OfInt dwYSize$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwYSize"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwYSize
		 * }
		 */
		public static final OfInt dwYSize$layout() {
			return dwYSize$LAYOUT;
		}

		private static final long dwYSize$OFFSET = 44;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwYSize
		 * }
		 */
		public static final long dwYSize$offset() {
			return dwYSize$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwYSize
		 * }
		 */
		public static int dwYSize(MemorySegment struct) {
			return struct.get(dwYSize$LAYOUT, dwYSize$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwYSize
		 * }
		 */
		public static void dwYSize(MemorySegment struct, int fieldValue) {
			struct.set(dwYSize$LAYOUT, dwYSize$OFFSET, fieldValue);
		}

		private static final OfInt dwXCountChars$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwXCountChars"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwXCountChars
		 * }
		 */
		public static final OfInt dwXCountChars$layout() {
			return dwXCountChars$LAYOUT;
		}

		private static final long dwXCountChars$OFFSET = 48;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwXCountChars
		 * }
		 */
		public static final long dwXCountChars$offset() {
			return dwXCountChars$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwXCountChars
		 * }
		 */
		public static int dwXCountChars(MemorySegment struct) {
			return struct.get(dwXCountChars$LAYOUT, dwXCountChars$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwXCountChars
		 * }
		 */
		public static void dwXCountChars(MemorySegment struct, int fieldValue) {
			struct.set(dwXCountChars$LAYOUT, dwXCountChars$OFFSET, fieldValue);
		}

		private static final OfInt dwYCountChars$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwYCountChars"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwYCountChars
		 * }
		 */
		public static final OfInt dwYCountChars$layout() {
			return dwYCountChars$LAYOUT;
		}

		private static final long dwYCountChars$OFFSET = 52;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwYCountChars
		 * }
		 */
		public static final long dwYCountChars$offset() {
			return dwYCountChars$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwYCountChars
		 * }
		 */
		public static int dwYCountChars(MemorySegment struct) {
			return struct.get(dwYCountChars$LAYOUT, dwYCountChars$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwYCountChars
		 * }
		 */
		public static void dwYCountChars(MemorySegment struct, int fieldValue) {
			struct.set(dwYCountChars$LAYOUT, dwYCountChars$OFFSET, fieldValue);
		}

		private static final OfInt dwFillAttribute$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwFillAttribute"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwFillAttribute
		 * }
		 */
		public static final OfInt dwFillAttribute$layout() {
			return dwFillAttribute$LAYOUT;
		}

		private static final long dwFillAttribute$OFFSET = 56;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwFillAttribute
		 * }
		 */
		public static final long dwFillAttribute$offset() {
			return dwFillAttribute$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwFillAttribute
		 * }
		 */
		public static int dwFillAttribute(MemorySegment struct) {
			return struct.get(dwFillAttribute$LAYOUT, dwFillAttribute$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwFillAttribute
		 * }
		 */
		public static void dwFillAttribute(MemorySegment struct, int fieldValue) {
			struct.set(dwFillAttribute$LAYOUT, dwFillAttribute$OFFSET, fieldValue);
		}

		private static final OfInt dwFlags$LAYOUT = (OfInt) $LAYOUT.select(groupElement("dwFlags"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * DWORD dwFlags
		 * }
		 */
		public static final OfInt dwFlags$layout() {
			return dwFlags$LAYOUT;
		}

		private static final long dwFlags$OFFSET = 60;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * DWORD dwFlags
		 * }
		 */
		public static final long dwFlags$offset() {
			return dwFlags$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * DWORD dwFlags
		 * }
		 */
		public static int dwFlags(MemorySegment struct) {
			return struct.get(dwFlags$LAYOUT, dwFlags$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * DWORD dwFlags
		 * }
		 */
		public static void dwFlags(MemorySegment struct, int fieldValue) {
			struct.set(dwFlags$LAYOUT, dwFlags$OFFSET, fieldValue);
		}

		private static final OfShort wShowWindow$LAYOUT = (OfShort) $LAYOUT.select(groupElement("wShowWindow"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * WORD wShowWindow
		 * }
		 */
		public static final OfShort wShowWindow$layout() {
			return wShowWindow$LAYOUT;
		}

		private static final long wShowWindow$OFFSET = 64;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * WORD wShowWindow
		 * }
		 */
		public static final long wShowWindow$offset() {
			return wShowWindow$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * WORD wShowWindow
		 * }
		 */
		public static short wShowWindow(MemorySegment struct) {
			return struct.get(wShowWindow$LAYOUT, wShowWindow$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * WORD wShowWindow
		 * }
		 */
		public static void wShowWindow(MemorySegment struct, short fieldValue) {
			struct.set(wShowWindow$LAYOUT, wShowWindow$OFFSET, fieldValue);
		}

		private static final OfShort cbReserved2$LAYOUT = (OfShort) $LAYOUT.select(groupElement("cbReserved2"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * WORD cbReserved2
		 * }
		 */
		public static final OfShort cbReserved2$layout() {
			return cbReserved2$LAYOUT;
		}

		private static final long cbReserved2$OFFSET = 66;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * WORD cbReserved2
		 * }
		 */
		public static final long cbReserved2$offset() {
			return cbReserved2$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * WORD cbReserved2
		 * }
		 */
		public static short cbReserved2(MemorySegment struct) {
			return struct.get(cbReserved2$LAYOUT, cbReserved2$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * WORD cbReserved2
		 * }
		 */
		public static void cbReserved2(MemorySegment struct, short fieldValue) {
			struct.set(cbReserved2$LAYOUT, cbReserved2$OFFSET, fieldValue);
		}

		private static final AddressLayout lpReserved2$LAYOUT = (AddressLayout) $LAYOUT
				.select(groupElement("lpReserved2"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * LPBYTE lpReserved2
		 * }
		 */
		public static final AddressLayout lpReserved2$layout() {
			return lpReserved2$LAYOUT;
		}

		private static final long lpReserved2$OFFSET = 72;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * LPBYTE lpReserved2
		 * }
		 */
		public static final long lpReserved2$offset() {
			return lpReserved2$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * LPBYTE lpReserved2
		 * }
		 */
		public static MemorySegment lpReserved2(MemorySegment struct) {
			return struct.get(lpReserved2$LAYOUT, lpReserved2$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * LPBYTE lpReserved2
		 * }
		 */
		public static void lpReserved2(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(lpReserved2$LAYOUT, lpReserved2$OFFSET, fieldValue);
		}

		private static final AddressLayout hStdInput$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("hStdInput"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hStdInput
		 * }
		 */
		public static final AddressLayout hStdInput$layout() {
			return hStdInput$LAYOUT;
		}

		private static final long hStdInput$OFFSET = 80;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hStdInput
		 * }
		 */
		public static final long hStdInput$offset() {
			return hStdInput$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hStdInput
		 * }
		 */
		public static MemorySegment hStdInput(MemorySegment struct) {
			return struct.get(hStdInput$LAYOUT, hStdInput$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hStdInput
		 * }
		 */
		public static void hStdInput(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hStdInput$LAYOUT, hStdInput$OFFSET, fieldValue);
		}

		private static final AddressLayout hStdOutput$LAYOUT = (AddressLayout) $LAYOUT
				.select(groupElement("hStdOutput"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hStdOutput
		 * }
		 */
		public static final AddressLayout hStdOutput$layout() {
			return hStdOutput$LAYOUT;
		}

		private static final long hStdOutput$OFFSET = 88;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hStdOutput
		 * }
		 */
		public static final long hStdOutput$offset() {
			return hStdOutput$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hStdOutput
		 * }
		 */
		public static MemorySegment hStdOutput(MemorySegment struct) {
			return struct.get(hStdOutput$LAYOUT, hStdOutput$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hStdOutput
		 * }
		 */
		public static void hStdOutput(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hStdOutput$LAYOUT, hStdOutput$OFFSET, fieldValue);
		}

		private static final AddressLayout hStdError$LAYOUT = (AddressLayout) $LAYOUT.select(groupElement("hStdError"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * HANDLE hStdError
		 * }
		 */
		public static final AddressLayout hStdError$layout() {
			return hStdError$LAYOUT;
		}

		private static final long hStdError$OFFSET = 96;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * HANDLE hStdError
		 * }
		 */
		public static final long hStdError$offset() {
			return hStdError$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * HANDLE hStdError
		 * }
		 */
		public static MemorySegment hStdError(MemorySegment struct) {
			return struct.get(hStdError$LAYOUT, hStdError$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * HANDLE hStdError
		 * }
		 */
		public static void hStdError(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(hStdError$LAYOUT, hStdError$OFFSET, fieldValue);
		}

		/**
		 * Obtains a slice of {@code arrayParam} which selects the array element at
		 * {@code index}. The returned segment has address
		 * {@code arrayParam.address() + index * layout().byteSize()}
		 */
		public static MemorySegment asSlice(MemorySegment array, long index) {
			return array.asSlice(layout().byteSize() * index);
		}

		/**
		 * The size (in bytes) of this struct
		 */
		public static long sizeof() {
			return layout().byteSize();
		}

		/**
		 * Allocate a segment of size {@code layout().byteSize()} using
		 * {@code allocator}
		 */
		public static MemorySegment allocate(SegmentAllocator allocator) {
			return allocator.allocate(layout());
		}

		/**
		 * Allocate an array of size {@code elementCount} using {@code allocator}. The
		 * returned segment has size {@code elementCount * layout().byteSize()}.
		 */
		public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
			return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
			return reinterpret(addr, 1, arena, cleanup);
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * elementCount * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena,
				Consumer<MemorySegment> cleanup) {
			return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
		}
	}

	public class _STARTUPINFOEXA {

		_STARTUPINFOEXA() {
			// Should not be called directly
		}

		private static final GroupLayout $LAYOUT = MemoryLayout
				.structLayout(STARTUPINFOA.layout().withName("StartupInfo"), C_POINTER.withName("lpAttributeList"))
				.withName("_STARTUPINFOEXA");

		/**
		 * The layout of this struct
		 */
		public static final GroupLayout layout() {
			return $LAYOUT;
		}

		private static final GroupLayout StartupInfo$LAYOUT = (GroupLayout) $LAYOUT.select(groupElement("StartupInfo"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * STARTUPINFOA StartupInfo
		 * }
		 */
		public static final GroupLayout StartupInfo$layout() {
			return StartupInfo$LAYOUT;
		}

		private static final long StartupInfo$OFFSET = 0;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * STARTUPINFOA StartupInfo
		 * }
		 */
		public static final long StartupInfo$offset() {
			return StartupInfo$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * STARTUPINFOA StartupInfo
		 * }
		 */
		public static MemorySegment StartupInfo(MemorySegment struct) {
			return struct.asSlice(StartupInfo$OFFSET, StartupInfo$LAYOUT.byteSize());
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * STARTUPINFOA StartupInfo
		 * }
		 */
		public static void StartupInfo(MemorySegment struct, MemorySegment fieldValue) {
			MemorySegment.copy(fieldValue, 0L, struct, StartupInfo$OFFSET, StartupInfo$LAYOUT.byteSize());
		}

		private static final AddressLayout lpAttributeList$LAYOUT = (AddressLayout) $LAYOUT
				.select(groupElement("lpAttributeList"));

		/**
		 * Layout for field:
		 * {@snippet lang = c : * LPPROC_THREAD_ATTRIBUTE_LIST lpAttributeList
		 * }
		 */
		public static final AddressLayout lpAttributeList$layout() {
			return lpAttributeList$LAYOUT;
		}

		private static final long lpAttributeList$OFFSET = 104;

		/**
		 * Offset for field:
		 * {@snippet lang = c : * LPPROC_THREAD_ATTRIBUTE_LIST lpAttributeList
		 * }
		 */
		public static final long lpAttributeList$offset() {
			return lpAttributeList$OFFSET;
		}

		/**
		 * Getter for field:
		 * {@snippet lang = c : * LPPROC_THREAD_ATTRIBUTE_LIST lpAttributeList
		 * }
		 */
		public static MemorySegment lpAttributeList(MemorySegment struct) {
			return struct.get(lpAttributeList$LAYOUT, lpAttributeList$OFFSET);
		}

		/**
		 * Setter for field:
		 * {@snippet lang = c : * LPPROC_THREAD_ATTRIBUTE_LIST lpAttributeList
		 * }
		 */
		public static void lpAttributeList(MemorySegment struct, MemorySegment fieldValue) {
			struct.set(lpAttributeList$LAYOUT, lpAttributeList$OFFSET, fieldValue);
		}

		/**
		 * Obtains a slice of {@code arrayParam} which selects the array element at
		 * {@code index}. The returned segment has address
		 * {@code arrayParam.address() + index * layout().byteSize()}
		 */
		public static MemorySegment asSlice(MemorySegment array, long index) {
			return array.asSlice(layout().byteSize() * index);
		}

		/**
		 * The size (in bytes) of this struct
		 */
		public static long sizeof() {
			return layout().byteSize();
		}

		/**
		 * Allocate a segment of size {@code layout().byteSize()} using
		 * {@code allocator}
		 */
		public static MemorySegment allocate(SegmentAllocator allocator) {
			return allocator.allocate(layout());
		}

		/**
		 * Allocate an array of size {@code elementCount} using {@code allocator}. The
		 * returned segment has size {@code elementCount * layout().byteSize()}.
		 */
		public static MemorySegment allocateArray(long elementCount, SegmentAllocator allocator) {
			return allocator.allocate(MemoryLayout.sequenceLayout(elementCount, layout()));
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, Arena arena, Consumer<MemorySegment> cleanup) {
			return reinterpret(addr, 1, arena, cleanup);
		}

		/**
		 * Reinterprets {@code addr} using target {@code arena} and
		 * {@code cleanupAction) (if any). The returned segment has size {@code
		 * elementCount * layout().byteSize()}
		 */
		public static MemorySegment reinterpret(MemorySegment addr, long elementCount, Arena arena,
				Consumer<MemorySegment> cleanup) {
			return addr.reinterpret(layout().byteSize() * elementCount, arena, cleanup);
		}
	}

	private Kernel32() {
	}

}
