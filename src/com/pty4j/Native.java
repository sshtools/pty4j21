package com.pty4j;

/*
 * The contents of this file is dual-licensed under 2
 * alternative Open Source/Free licenses: LGPL 2.1 or later and
 * Apache License 2.0. (starting with JNA version 4.0.0).
 *
 * You can freely decide which license you want to apply to
 * the project.
 *
 * You may obtain a copy of the LGPL License at:
 *
 * http://www.gnu.org/licenses/licenses.html
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "LGPL2.1".
 *
 * You may obtain a copy of the Apache License at:
 *
 * http://www.apache.org/licenses/
 *
 * A copy is also included in the downloadable source code package
 * containing JNA, in file "AL2.0".
 */

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.foreign.AddressLayout;
import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

public class Native {

	
	public static final AddressLayout C_POINTER = ValueLayout.ADDRESS
	.withTargetLayout(MemoryLayout.sequenceLayout(java.lang.Long.MAX_VALUE, JAVA_BYTE));
	
	private static final Logger LOG = System.getLogger(Native.class.getName());
	
    private static final Map<String, List<String>> searchPaths = new ConcurrentHashMap<>();
    private static final LinkedHashSet<String> librarySearchPath = new LinkedHashSet<>();
	
	static {
        if (System.getProperty("jna.platform.library.path") == null
            && !Platform.isWindows()) {
            // Add default path lookups for unix-like systems
            String platformPath = "";
            String sep = "";
            String archPath = "";

            //
            // Search first for an arch specific path if one exists, but always
            // include the generic paths if they exist.
            // NOTES (wmeissner):
            // Some older linux amd64 distros did not have /usr/lib64, and
            // 32bit distros only have /usr/lib.  FreeBSD also only has
            // /usr/lib by default, with /usr/lib32 for 32bit compat.
            // Solaris seems to have both, but defaults to 32bit userland even
            // on 64bit machines, so we have to explicitly search the 64bit
            // one when running a 64bit JVM.
            //
            if (Platform.isLinux() || Platform.isSolaris()
                || Platform.isFreeBSD() || Platform.iskFreeBSD()) {
                // Linux & FreeBSD use /usr/lib32, solaris uses /usr/lib/32
                archPath = (Platform.isSolaris() ? "/" : "") + Native.C_POINTER.byteSize() * 8;
            }
            String[] paths = {
                "/usr/lib" + archPath,
                "/lib" + archPath,
                "/usr/lib",
                "/lib",
            };
            // Multi-arch support on Ubuntu (and other
            // multi-arch distributions)
            // paths is scanned against real directory
            // so for platforms which are not multi-arch
            // this should continue to work.
            if (Platform.isLinux() || Platform.iskFreeBSD() || Platform.isGNU()) {
                String multiArchPath = getMultiArchPath();

                // Assemble path with all possible options
                paths = new String[] {
                    "/usr/lib/" + multiArchPath,
                    "/lib/" + multiArchPath,
                    "/usr/lib" + archPath,
                    "/lib" + archPath,
                    "/usr/lib",
                    "/lib",
                };
            }

            // We might be wrong with the multiArchPath above. Raspbian,
            // the Raspberry Pi flavor of Debian, for example, uses
            // uses arm-linux-gnuabihf since it's using the hard-float
            // ABI for armv6. Other distributions might use a different
            // tuple for the same thing. Query ldconfig to get the additional
            // library paths it knows about.
            if (Platform.isLinux()) {
                ArrayList<String> ldPaths = getLinuxLdPaths();
                // prepend the paths we already have
                for (int i=paths.length-1; 0 <= i; i--) {
                    int found = ldPaths.indexOf(paths[i]);
                    if (found != -1) {
                        ldPaths.remove(found);
                    }
                    ldPaths.add(0, paths[i]);
                }
                paths = ldPaths.toArray(new String[0]);
            }

            for (int i=0;i < paths.length;i++) {
                File dir = new File(paths[i]);
                if (dir.exists() && dir.isDirectory()) {
                    platformPath += sep + paths[i];
                    sep = File.pathSeparator;
                }
            }
            if (!"".equals(platformPath)) {
                System.setProperty("jna.platform.library.path", platformPath);
            }
        }
        librarySearchPath.addAll(initPaths("jna.platform.library.path"));
    }
	
	public final static int TRUE = 1;
	public final static int FALSE = 0;
	
	/**
     * Add a path to search for the specified library, ahead of any system
     * paths.  This is similar to setting <code>pty4j.library.path</code>, but
     * only extends the search path for a single library.
     *
     * @param libraryName The name of the library to use the path for
     * @param path The path to use when trying to load the library
     */
    public static final void addSearchPath(String libraryName, String path) {
        List<String> customPaths = searchPaths.get(libraryName);
        if (customPaths == null) {
            customPaths = Collections.synchronizedList(new ArrayList<String>());
            searchPaths.put(libraryName, customPaths);
        }

        customPaths.add(path);
    } 
	
	public static boolean err(int res) {
		return res == FALSE;
	}

	
	public static SymbolLookup load(Path libraryPath, final String libraryName, Arena arena) {
		try {
			return SymbolLookup.libraryLookup(libraryPath, arena);
		}
		catch(IllegalArgumentException iae) {
			return load(libraryName, arena);
		}
	}
	
	public static SymbolLookup load(final String libraryName, Arena arena) {
        LOG.log(Level.DEBUG, "Looking for library '" + libraryName + "'");

        List<Throwable> exceptions = new ArrayList<>();
        boolean isAbsolutePath = new File(libraryName).isAbsolute();
        LinkedHashSet<String> searchPath = new LinkedHashSet<>();

        //
        // Prepend any custom search paths specifically for this library
        //
        List<String> customPaths = searchPaths.get(libraryName);
        if (customPaths != null) {
            synchronized (customPaths) {
                searchPath.addAll(customPaths);
            }
        }

        LOG.log(Level.DEBUG, "Adding paths from pty4j.library.path: " + System.getProperty("pty4j.library.path"));

        searchPath.addAll(initPaths("pty4j.library.path"));
        String libraryPath = findLibraryPath(libraryName, searchPath);
        SymbolLookup handle = null;
        //
        // Only search user specified paths first.  This will also fall back
        // to dlopen/LoadLibrary() since findLibraryPath returns the mapped
        // name if it cannot find the library.
        //
        try {
            LOG.log(Level.DEBUG, "Trying " + libraryPath);
            handle = lookup(arena, libraryPath);
        } catch(IllegalArgumentException e) {
            // Add the system paths back for all fallback searching
            LOG.log(Level.DEBUG, "Loading failed with message: " + e.getMessage());
            LOG.log(Level.DEBUG, "Adding system paths: " + librarySearchPath);
            exceptions.add(e);
            searchPath.addAll(librarySearchPath);
        }

        try {
            if (handle == null) {
                libraryPath = findLibraryPath(libraryName, searchPath);
                LOG.log(Level.DEBUG, "Trying " + libraryPath);
                handle = lookup(arena, libraryPath);
            }
        } catch(IllegalArgumentException ule) {
            LOG.log(Level.DEBUG, "Loading failed with message: " + ule.getMessage());
            exceptions.add(ule);
            // For android, try to "preload" the library using
            // System.loadLibrary(), which looks into the private /data/data
            // path, not found in any properties
            if (Platform.isAndroid()) {
                try {
                    LOG.log(Level.DEBUG, "Preload (via System.loadLibrary) " + libraryName);
                    System.loadLibrary(libraryName);
                    handle = lookup(arena, libraryPath);
                }
                catch(UnsatisfiedLinkError | IllegalArgumentException e2) {
                    LOG.log(Level.DEBUG, "Loading failed with message: " + e2.getMessage());
                    exceptions.add(e2);
                }
            }
            else if (Platform.isLinux() || Platform.isFreeBSD()) {
                //
                // Failed to load the library normally - try to match libfoo.so.*
                //
                LOG.log(Level.DEBUG, "Looking for version variants");
                libraryPath = matchLibrary(libraryName, searchPath);
                if (libraryPath != null) {
                    LOG.log(Level.DEBUG, "Trying " + libraryPath);
                    try {
                        handle = lookup(arena, libraryPath);
                    }
                    catch(IllegalArgumentException e2) {
                        LOG.log(Level.DEBUG, "Loading failed with message: " + e2.getMessage());
                        exceptions.add(e2);
                    }
                }
            }
            // Search framework libraries on OS X
            else if (Platform.isMac() && !libraryName.endsWith(".dylib")) {
                for(String frameworkName : matchFramework(libraryName)) {
                    try {
                        LOG.log(Level.DEBUG, "Trying " + frameworkName);
                        handle = lookup(arena, frameworkName);
                        break;
                    }
                    catch(IllegalArgumentException e2) {
                        LOG.log(Level.DEBUG, "Loading failed with message: " + e2.getMessage());
                        exceptions.add(e2);
                    }
                }
            }
            // Try the same library with a "lib" prefix
            else if (Platform.isWindows() && !isAbsolutePath) {
                LOG.log(Level.DEBUG, "Looking for lib- prefix");
                libraryPath = findLibraryPath("lib" + libraryName, searchPath);
                if (libraryPath != null) {
                    LOG.log(Level.DEBUG, "Trying " + libraryPath);
                    try {
                        handle = lookup(arena, libraryPath);
                    } catch(IllegalArgumentException e2) {
                        LOG.log(Level.DEBUG, "Loading failed with message: " + e2.getMessage());
                        exceptions.add(e2);
                    }
                }
            }
            // As a last resort, try to extract the library from the class
            // path, using the current context class loader.
//            if (handle == 0) {
//                try {
//                    File embedded = Native.extractFromResourcePath(libraryName, (ClassLoader)options.get(Library.OPTION_CLASSLOADER));
//                    if (embedded != null) {
//                        try {
//                            handle = Native.open(embedded.getAbsolutePath(), openFlags);
//                            libraryPath = embedded.getAbsolutePath();
//                        } finally {
//                            // Don't leave temporary files around
//                            if (Native.isUnpacked(embedded)) {
//                                Native.deleteLibrary(embedded);
//                            }
//                        }
//                    }
//                }
//                catch(IOException e2) {
//                    LOG.log(Level.DEBUG, "Loading failed with message: " + e2.getMessage());
//                    exceptions.add(e2);
//                }
//            }

            if (handle == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("Unable to load library '");
                sb.append(libraryName);
                sb.append("':");
                for(Throwable t: exceptions) {
                    sb.append("\n");
                    sb.append(t.getMessage());
                }
                throw new IllegalArgumentException(sb.toString());
            }
        }

        LOG.log(Level.DEBUG, "Found library '" + libraryName + "' at " + libraryPath);
        return handle;
    }

	protected static SymbolLookup lookup(Arena arena, String libraryPath) {
		var path = Paths.get(libraryPath);
		if(path.isAbsolute())
			return SymbolLookup.libraryLookup(path, arena);
		else
			return SymbolLookup.libraryLookup(path.getFileName().toString(), arena);
	}
	
	public static boolean ok(int res) {
		return res == TRUE;
	}
	
	public static MemorySegment toCString(SegmentAllocator allocator, String s, Charset charset) {
	    // "==" is OK here as StandardCharsets.UTF_8 == Charset.forName("UTF8")
	    if (StandardCharsets.UTF_8 == charset)
	        return allocator.allocateFrom(s);

	    // else if (StandardCharsets.UTF_16LE == charset) {
	    //     return Holger answer
	    // }

	    // For MB charsets it is safer to append terminator '\0' and let JDK append
	    // appropriate byte[] null termination (typically 1,2,4 bytes) to the segment
	    return allocator.allocateFrom(ValueLayout.JAVA_BYTE, (s+"\0").getBytes(charset));
	}

	public static String toJavaString(MemorySegment wide) {
		return toJavaString(wide, StandardCharsets.UTF_16LE);
	}
	public static String toJavaString(MemorySegment segment, Charset charset) {
		return toJavaString(segment, charset, -1);
	}
	
	public static String toJavaString(MemorySegment segment, Charset charset, int maxLen) {
		// JDK Panama only handles UTF-8, it does strlen() scan for 0 in the segment
		// which is valid as all code points of 2 and 3 bytes lead with high bit "1".
		if (StandardCharsets.UTF_8 == charset)
			return segment.getString(0);

		// if (StandardCharsets.UTF_16LE == charset) {
		// return Holger answer
		// }

		// This conversion is convoluted: MemorySegment->ByteBuffer->CharBuffer->String
		// segment might be a pointer (so massive byteSize()), so restrict to provided length
		CharBuffer cb = charset.decode(maxLen == -1 ? segment.asByteBuffer() : segment.asSlice(0, Math.min(maxLen, segment.byteSize())).asByteBuffer());

		// cb.array() isn't valid unless cb.hasArray() is true so use cb.get() to
		// find a null terminator character, ignoring it and the remaining characters
		final int max = cb.limit();
		int len = 0;
		while (len < max && cb.get(len) != '\0')
			len++;

		return cb.limit(len).toString();
	}

	public static String toJavaString(MemorySegment wide, int maxLen) {
		return toJavaString(wide, StandardCharsets.UTF_16LE, maxLen);
	}

	/** Convert Java String to Windows Wide String format */
	public static MemorySegment toWideString(String s, SegmentAllocator allocator) {
	    return toCString(allocator, s, StandardCharsets.UTF_16LE);
	}

    /** Similar to {@link System#mapLibraryName}, except that it maps to
        standard shared library formats rather than specifically JNI formats.
        @param libName base (undecorated) name of library
    */
    static String mapSharedLibraryName(String libName) {
        if (Platform.isMac()) {
            if (libName.startsWith("lib")
                && (libName.endsWith(".dylib")
                    || libName.endsWith(".jnilib"))) {
                return libName;
            }
            String name = System.mapLibraryName(libName);
            // On MacOSX, System.mapLibraryName() returns the .jnilib extension
            // (the suffix for JNI libraries); ordinarily shared libraries have
            // a .dylib suffix
            if (name.endsWith(".jnilib")) {
                return name.substring(0, name.lastIndexOf(".jnilib")) + ".dylib";
            }
            return name;
        }
        else if (Platform.isLinux() || Platform.isFreeBSD()) {
            if (isVersionedName(libName) || libName.endsWith(".so")) {
                // A specific version was requested - use as is for search
                return libName;
            }
        }
        else if (Platform.isAIX()) {    // can be libx.a, libx.a(shr.o), libx.so
            if (isVersionedName(libName) || libName.endsWith(".so") || libName.startsWith("lib") || libName.endsWith(".a")) {
                // A specific version was requested - use as is for search
                return libName;
            }
        }
        else if (Platform.isWindows()) {
            if (libName.endsWith(".drv") || libName.endsWith(".dll") || libName.endsWith(".ocx")) {
                return libName;
            }
        }

        String mappedName = System.mapLibraryName(libName);
        if(Platform.isAIX() && mappedName.endsWith(".so")) {
            return mappedName.replaceAll(".so$", ".a");
        } else {
            return mappedName;
        }
    }

    /** Look for a matching framework (OSX) */
    static String[] matchFramework(String libraryName) {
        Set<String> paths = new LinkedHashSet<>();
        File framework = new File(libraryName);
        if (framework.isAbsolute()) {
            if (libraryName.contains(".framework")) {
                if (framework.exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            }
            else {
                framework = new File(new File(framework.getParentFile(), framework.getName() + ".framework"), framework.getName());
                if (framework.exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            }
        }
        else {
            final String[] PREFIXES = { System.getProperty("user.home"), "", "/System" };
            String suffix = !libraryName.contains(".framework")
                    ? libraryName + ".framework/" + libraryName : libraryName;
            for (String prefix : PREFIXES) {
                framework = new File(prefix + "/Library/Frameworks/" + suffix);
                if (framework.exists()) {
                    return new String[]{framework.getAbsolutePath()};
                }
                paths.add(framework.getAbsolutePath());
            }
        }
        return paths.toArray(new String[0]);
    }



    /**
     * matchLibrary() is very Linux specific.  It is here to deal with the case
     * where /usr/lib/libc.so does not exist, or it is not a valid symlink to
     * a versioned file (e.g. /lib/libc.so.6).
     */
    static String matchLibrary(final String libName, Collection<String> searchPath) {
        File lib = new File(libName);
        if (lib.isAbsolute()) {
            searchPath = Arrays.asList(lib.getParent());
        }
        FilenameFilter filter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    return (filename.startsWith("lib" + libName + ".so")
                            || (filename.startsWith(libName + ".so")
                                && libName.startsWith("lib")))
                        && isVersionedName(filename);
                }
            };

        Collection<File> matches = new LinkedList<>();
        for (String path : searchPath) {
            File[] files = new File(path).listFiles(filter);
            if (files != null && files.length > 0) {
                matches.addAll(Arrays.asList(files));
            }
        }

        //
        // Search through the results and return the highest numbered version
        // i.e. libc.so.6 is preferred over libc.so.5
        double bestVersion = -1;
        String bestMatch = null;
        for (File f : matches) {
            String path = f.getAbsolutePath();
            String ver = path.substring(path.lastIndexOf(".so.") + 4);
            double version = parseVersion(ver);
            if (version > bestVersion) {
                bestVersion = version;
                bestMatch = path;
            }
        }
        return bestMatch;
    }

    static double parseVersion(String ver) {
        double v = 0;
        double divisor = 1;
        int dot = ver.indexOf(".");
        while (ver != null) {
            String num;
            if (dot != -1) {
                num = ver.substring(0, dot);
                ver = ver.substring(dot + 1);
                dot = ver.indexOf(".");
            }
            else {
                num = ver;
                ver = null;
            }
            try {
                v += Integer.parseInt(num) / divisor;
            }
            catch(NumberFormatException e) {
                return 0;
            }
            divisor *= 100;
        }

        return v;
    }

    /** Use standard library search paths to find the library. */
    private static String findLibraryPath(String libName, Collection<String> searchPath) {

        //
        // If a full path to the library was specified, don't search for it
        //
        if (new File(libName).isAbsolute()) {
            return libName;
        }

        //
        // Get the system name for the library (e.g. libfoo.so)
        //
        String name = mapSharedLibraryName(libName);

        // Search in the JNA paths for it
        for (String path : searchPath) {
            File file = new File(path, name);
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            if (Platform.isMac()) {
                // Native libraries delivered via JNLP class loader
                // may require a .jnilib extension to be found
                if (name.endsWith(".dylib")) {
                    file = new File(path, name.substring(0, name.lastIndexOf(".dylib")) + ".jnilib");
                    if (file.exists()) {
                        return file.getAbsolutePath();
                    }
                }
            }
        }

        //
        // Default to returning the mapped library name and letting the system
        // search for it
        //
        return name;
    }

    private static List<String> initPaths(String key) {
        String value = System.getProperty(key, "");
        if ("".equals(value)) {
            return Collections.emptyList();
        }
        StringTokenizer st = new StringTokenizer(value, File.pathSeparator);
        List<String> list = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String path = st.nextToken();
            if (!"".equals(path)) {
                list.add(path);
            }
        }
        return list;
    } 
    
    /**
     * Get the library paths from ldconfig cache. Tested against ldconfig 2.13.
     */
    private static ArrayList<String> getLinuxLdPaths() {
        ArrayList<String> ldPaths = new ArrayList<>();
        Process process = null;
        BufferedReader reader = null;
        try {
            process = Runtime.getRuntime().exec("/sbin/ldconfig -p");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String buffer;
            while ((buffer = reader.readLine()) != null) {
                int startPath = buffer.indexOf(" => ");
                int endPath = buffer.lastIndexOf('/');
                if (startPath != -1 && endPath != -1 && startPath < endPath) {
                    String path = buffer.substring(startPath + 4, endPath);
                    if (!ldPaths.contains(path)) {
                        ldPaths.add(path);
                    }
                }
            }
        } catch (Exception e) {
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                }
            }
            if(process != null) {
                try {
                    process.waitFor();
                } catch (InterruptedException e) {
                }
            }
        }
        return ldPaths;
    }

	private static boolean isVersionedName(String name) {
        if (name.startsWith("lib")) {
            int so = name.lastIndexOf(".so.");
            if (so != -1 && so + 4 < name.length()) {
                for (int i=so+4;i < name.length();i++) {
                    char ch = name.charAt(i);
                    if (!Character.isDigit(ch) && ch != '.') {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
	private static String getMultiArchPath() {
        String cpu = Platform.ARCH;
        String kernel = Platform.iskFreeBSD()
            ? "-kfreebsd"
            : (Platform.isGNU() ? "" : "-linux");
        String libc = "-gnu";

        if (Platform.isIntel()) {
            cpu = (Platform.is64Bit() ? "x86_64" : "i386");
        }
        else if (Platform.isPPC()) {
            cpu = (Platform.is64Bit() ? "powerpc64" : "powerpc");
        }
        else if (Platform.isARM()) {
            cpu = "arm";
            libc = "-gnueabi";
        }
        else if (Platform.ARCH.equals("mips64el")) {
            libc = "-gnuabi64";
        }

        return cpu + kernel + libc;
    }
}
