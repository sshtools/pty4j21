package com.pty4j.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class ExtractedNative {

  private static final Logger LOG = System.getLogger(ExtractedNative.class.getName());
  
  static final String[] LOCATIONS = {
      "darwin/libpty.dylib",
      "freebsd/x86/libpty.so",
      "freebsd/x86-64/libpty.so",
      "linux/x86/libpty.so",
      "linux/x86-64/libpty.so",
      "linux/aarch64/libpty.so",
      "linux/arm/libpty.so",
      "linux/ppc64le/libpty.so",
      "linux/mips64el/libpty.so",
      "win/aarch64/conpty.dll",
      "win/aarch64/OpenConsole.exe",
      "win/aarch64/win-helper.dll",
      "win/aarch64/winpty-agent.exe",
      "win/aarch64/winpty.dll",
      "win/x86/winpty-agent.exe",
      "win/x86/winpty.dll",
      "win/x86-64/conpty.dll",
      "win/x86-64/OpenConsole.exe",
      "win/x86-64/cyglaunch.exe",
      "win/x86-64/win-helper.dll",
      "win/x86-64/winpty-agent.exe",
      "win/x86-64/winpty.dll"
  };
  //static final String DEFAULT_RESOURCE_NAME_PREFIX = "resources/com/pty4j/native/";
  static final String DEFAULT_RESOURCE_NAME_PREFIX = "";

  private static final ExtractedNative INSTANCE = new ExtractedNative();
  private String myResourceOsArchSubPath;
  private String myResourceNamePrefix;
  private boolean myInitialized;
  private volatile File myDestDir;

  private ExtractedNative() {
    this(null, null);
  }

  ExtractedNative(@Nullable String resourceOsArchSubPath, @Nullable String resourceNamePrefix) {
    myResourceOsArchSubPath = resourceOsArchSubPath;
    myResourceNamePrefix = resourceNamePrefix;
  }

  @NotNull
  public static ExtractedNative getInstance() {
    return INSTANCE;
  }

  @NotNull
  File getDestDir() {
    if (!myInitialized) {
      init();
    }
    return myDestDir;
  }

  private void init() {
    try {
      myResourceOsArchSubPath = Objects.requireNonNullElse(myResourceOsArchSubPath, PtyUtil.getNativeLibraryOsArchSubPath());
      myResourceNamePrefix = Objects.requireNonNullElse(myResourceNamePrefix, DEFAULT_RESOURCE_NAME_PREFIX);
      synchronized (this) {
        if (!myInitialized) {
          doInit();
        }
        myInitialized = true;
      }
    } catch (Exception e) {
      throw new IllegalStateException("Cannot extract pty4j native " + myResourceOsArchSubPath, e);
    }
  }

  private void doInit() throws IOException {
    long startTimeNano = System.nanoTime();
    Path destDir = getOrCreateDestDir();
    if (LOG.isLoggable(Level.DEBUG)) {
      LOG.log(Level.DEBUG, "Found {0} in {1}", destDir, pastTime(startTimeNano));
    }
    List<Path> children;
    try (Stream<Path> stream = Files.list(destDir)) {
      children = stream.collect(Collectors.<Path>toList());
    }
    if (LOG.isLoggable(Level.DEBUG)) {
      LOG.log(Level.DEBUG,"Listed files in {0}", pastTime(startTimeNano));
    }
    Map<String, Path> resourceToFileMap = new HashMap<>();
    for (Path child : children) {
      String resourceName = getResourceName(child.getFileName().toString());
      resourceToFileMap.put(resourceName, child);
    }
    Set<String> bundledResourceNames = getBundledResourceNames();
    boolean upToDate = isUpToDate(bundledResourceNames, resourceToFileMap);
    if (LOG.isLoggable(Level.DEBUG)) {
      LOG.log(Level.DEBUG, "Checked upToDate in {0}", pastTime(startTimeNano));
    }
    if (!upToDate) {
      for (Path child : children) {
        Files.delete(child);
      }
      if (LOG.isLoggable(Level.DEBUG)) {
    	LOG.log(Level.DEBUG, "Cleared directory in {0}", pastTime(startTimeNano));
      }
      for (String bundledResourceName : bundledResourceNames) {
        copy(bundledResourceName, destDir);
      }
      if (LOG.isLoggable(Level.DEBUG)) {
    	  LOG.log(Level.DEBUG, "Copied {0} in {1}",  bundledResourceNames, pastTime(startTimeNano));
      }
    }
    myDestDir = destDir.toFile();
    LOG.log(Level.INFO, "Extracted pty4j native in {0}", pastTime(startTimeNano));
  }

  @NotNull
  private Path getOrCreateDestDir() throws IOException {
    String staticParentDirPath = System.getProperty("pty4j.tmpdir");
    String prefix = "pty4j-" + myResourceOsArchSubPath.replace('/', '-');
    if (staticParentDirPath != null && !staticParentDirPath.trim().isEmpty()) {
      // It's assumed that "pty4j.tmpdir" directory should not be used by several processes with pty4j simultaneously.
      // And several pty4j.jar versions can't coexist in classpath of a process.
      Path staticParentDir = Paths.get(staticParentDirPath);
      if (staticParentDir.isAbsolute()) {
        Path staticDir = staticParentDir.resolve(prefix);
        if (Files.isDirectory(staticDir)) {
          return staticDir;
        }
        if (Files.isDirectory(staticParentDir)) {
          if (Files.exists(staticDir)) {
            Files.delete(staticDir);
          }
          return Files.createDirectory(staticDir);
        }
      }
    }
    Path tempDirectory = Files.createTempDirectory(prefix + "-");
    tempDirectory.toFile().deleteOnExit();
    return tempDirectory;
  }

  private boolean isUpToDate(@NotNull Set<String> bundledResourceNames, @NotNull Map<String, Path> resourceToFileMap) {
    if (!bundledResourceNames.equals(resourceToFileMap.keySet())) {
      return false;
    }
    for (Map.Entry<String, Path> entry : resourceToFileMap.entrySet()) {
      try {
        URL bundledUrl = getBundledResourceUrl(entry.getKey());
        byte[] bundledContentChecksum = md5(bundledUrl.openStream());
        byte[] fileContentChecksum = md5(Files.newInputStream(entry.getValue()));
        if (!Arrays.equals(bundledContentChecksum, fileContentChecksum)) {
          return false;
        }
      } catch (Exception e) {
    	LOG.log(Level.ERROR, "Cannot compare md5 checksums", e);
        return false;
      }
    }
    return true;
  }

  private static byte[] md5(@NotNull InputStream in) throws IOException, NoSuchAlgorithmException {
    try {
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      byte[] buffer = new byte[8192];
      int bufferSize;
      while ((bufferSize = in.read(buffer)) >= 0) {
        md5.update(buffer, 0, bufferSize);
      }
      return md5.digest();
    } finally {
      try {
        in.close();
      } catch (IOException e) {
        LOG.log(Level.ERROR, "Cannot close", e);
      }
    }
  }

  private @NotNull Set<String> getBundledResourceNames() {
    //noinspection Convert2Diamond
    Set<String> resourceNames = new HashSet<String>();
    String prefix = myResourceOsArchSubPath + "/";
    for (String location : LOCATIONS) {
      if (location.startsWith(prefix)) {
        resourceNames.add(myResourceNamePrefix + location);
      }
    }
    return resourceNames;
  }

  private @NotNull String getResourceName(@NotNull String fileName) {
    return ( myResourceNamePrefix == null ? "" : myResourceNamePrefix ) + myResourceOsArchSubPath + "/" + fileName;
  }

  private void copy(@NotNull String resourceName, @NotNull Path destDir) throws IOException {
    URL url = getBundledResourceUrl(resourceName);
    int lastNameInd = resourceName.lastIndexOf('/');
    String name = lastNameInd != -1 ? resourceName.substring(lastNameInd + 1) : resourceName;
    InputStream inputStream = url.openStream();
    //noinspection TryFinallyCanBeTryWithResources
    try {
      Files.copy(inputStream, destDir.resolve(name));
    }
    finally {
      inputStream.close();
    }
  }

  @NotNull
  private URL getBundledResourceUrl(@NotNull String resourceName) throws IOException {
    ClassLoader classLoader = ExtractedNative.class.getClassLoader();
    URL url = classLoader.getResource(resourceName);
    if (url == null) {
      ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
      if (contextClassLoader != null) {
        url = contextClassLoader.getResource(resourceName);
      }
      if (url == null) {
        throw new IOException("Unable to load " + resourceName);
      }
    }
    return url;
  }

  @NotNull
  private static String pastTime(long startTimeNano) {
    return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTimeNano) + " ms";
  }
}
