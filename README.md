# Pty4J21 - Pseudo terminal(PTY) implementation in Java 21+.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sshtools/pty4j21/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sshtools/jini)
[![Coverage Status](https://coveralls.io/repos/github/brett-samith/pty4j/badge.svg)](https://coveralls.io/github/brett-smith/pty4j)
[![javadoc](https://javadoc.io/badge2/com.sshtools/pty4j21/javadoc.svg)](https://javadoc.io/doc/com.sshtools/pty4j21)
![JPMS](https://img.shields.io/badge/JPMS-com.sshtools.pty4j-purple) 

![Pty4J21](banner.png) 

This is a Java implementation of PTY, forked from [Pty4J](https://github.com/JetBrains/pty4j). Written for Java 21+, and making use of the [Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/21/core/foreign-function-and-memory-api.html) together with the binrary helpers and libraries from the upstream project.

While [Pty4J](https://github.com/JetBrains/pty4j) is pretty good it suffers from a few issues, and Java has more moved on. See [More About This Fork](#more-about-this-fork) for a more detailed rationale.

### Features

In addition to everything provided by [upstream](https://github.com/JetBrains/pty4j),
 
 * Uses same binary helper DLLs and executables as upstream (these have not been rebuilt)
 * Zero external run-time dependencies, other than Java 21+.
 * In theory, should be faster than JNA based upstream. Further optimisation of FFM API is expected.
 * JPMS compliant.
 * Linux, Mac and Windows supported (and FreeBSD, untested).
 * Complete upstream API compatibility except for a couple of optional additions.
 * `PTYInputStrream, fastRead()` method for zero-copies of incoming PTY data.
 * `PTYProcess.getDisplayName()` public API method to get the pty name if known.

### Known Issues

 * Cygwin mode does not work, but then it doesn't seem to work for me using the upstream project either. Maybe I'm using wrong!
 * FreeBSD support should in theory work, but I do not currently have the facility to test. Feedback welcome.

## Dependencies

The legacy Windows pty implementation used here is the magnificent WinPty library written by Ryan Prichard: https://github.com/rprichard/winpty

## Adding Pty4J21 to your build

The releases are published to Maven Central: [com.sshtools:pty4j21](https://search.maven.org/artifact/com.sshtools/pty4j21).

### Maven

```xml
<dependency>
  <groupId>com.sshtools</groupId>
  <artifactId>pty4j21</artifactId>
  <version>0.21.0</version>
</dependency>
```

### Gradle

```groovy
dependencies {
  implementation 'com.sshtools:pty4j:0.21.0'
}
```

### Java 21

There are additional build-time and run-time requirements for Java 21 when native code is called.

Firstly, preview features must be enabled for compiling and unit tests. Exactly how this is done will depend on your environment, but to do this in Maven for example, add configuration to the compiler plugin and junit plugin.

```xml
<plugin>
	<groupId>org.apache.maven.plugins</groupId>
	<artifactId>maven-compiler-plugin</artifactId>
	<configuration>
		<enablePreview>true</enablePreview>
	</configuration>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>--enable-preview --enable-native-access=com.sshtools.pty4j</argLine>
    </configuration>
</plugin>
```

At run-time, ensure you add  `--enable-preview --enable-native-access=com.sshtools.pty4j` runtime arguments.
If you are building an executable jar, you can add the `Enable-Native-Access` manifest attribute instead.

As from Java 22, you can remove the `--enable-preview` flag. 
 

## Usage

Using this library is relatively easy:

```java
var cmd = { "/bin/sh", "-l" };
var env = new HashMap<>(System.getenv());
env.put("TERM", "xterm");
var process = new PtyProcessBuilder().setCommand(cmd).setEnvironment(env).start();

var os = process.getOutputStream();
var is = process.getInputStream();

// ... work with the streams ...

// wait until the PTY child process is terminated
var result = process.waitFor();
```

The operating systems currently supported by pty4j are: Linux, OSX, Windows and FreeBSD.

## License

The code in this library is licensed under Eclipse Public License, version 
1.0 and can be found online at: <http://www.eclipse.org/legal/epl-v10.html>.

## More About This Fork

This is an experimental, fork of the official [Pty4J](https://github.com/JetBrains/pty4j) that does **not** use JNA, PureJavaComm, Kotlin or SLF4J. Instead, it just requires Java 21 and uses a preview of the [Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/21/core/foreign-function-and-memory-api.html).

This fork came about as I found Pty4J to be quite slow due to JNA use, taking up roughly 50% of all CPU time in my terminal emulator [Pretty](https://github.com/sshtools/pretty). In order to support bitmap graphics over the pty (for example using [Notcurses](https://notcurses.com/), the speed of the pty becomes way more important. It is hoped this experiment will give me a significant performance boost.

As well as performance increases afforded by FFM's faster bindings, a `fastRead()` method has been
been added to `PTYInputStream` that accepts a `MemorySegment`. This represents and provides access to a segment of off-heap memory, which may be created in the calling application and passed to Pty4J for reading data from the tty. The calling application may then read from this segment with no additional copies.

The other major improvement was to reduce the number of run-time dependencies to zero. This was mostly a natural result of having to re-implement supporting libraries such as [PureJavaComm](https://github.com/nyholku/purejavacomm/), and with JNA being replaced by FFMAPI. Along with many others, I have long suffered [this bug](https://github.com/JetBrains/pty4j/issues/105), that appears to be forgotten. This issue just goes away with this fork.  That left SFL4J, which has been replaced by `java.lang.System.Logger`, and Kotlin.

The removal of Kotlin may be more controversial. There were multiple reasons for this.

 * I personally am not very familiar with Kotlin, it is not something I use day to day. It was going to be hard enough to learn FFMAPI without having to learn another language to complete this task. 
 * The mixed language nature of this project did not work so well in my chosen IDE, Eclipse. I am aware there are Kotlin plugins but they did not work out-of-the-box for me with Java 21.
 * I see little reason for this slow change to Kotlin in the first place, particularly with the rapidly improving Java language. This introduces an entirely new language runtime just to provide a library with a very simple use case. It *looks* like maybe upstream will eventually rewrite the whole thing, I am not so sure as a consumer of this library I am on board with this.
 
If anyone can tell me *why* Kotlin (and SLF4J for that matter) are required for this project, please convince me! :)

**Pty4J21** continues to make use of the great upstream work by the folks at JetBrains in the various work-arounds and native parts required to get a PTY on Java, in fact these have not changed at all, and are still the same binaries. The public API also has not change, but the Java side has had a fairly major re-write.  

The build was also recently changed to Kotlin. I am fine with that, but I cannot make it work with Java 21 and enabling preview features. Some weird error about operator ambiguity that I wasted too much time on. To make my life easier, a Maven POM has been added. This also lets me publish more easily to Maven Central and work in an environment I am way more familiar with.

Efforts will be made to keep this fork synchronized with upstream. It is likely most bug fixes would be applicable, but new features that turn up would be treated case by case. However, It is likely all but the simplest changes would require re-implementing.

 
### How Was This Achieved

The creation of this fork had a secondary, personal purpose. I was very interested in learning about FFMAPI, and this provided the ideal opportunity.

Many Java developers have probably at some point had some interaction with libraries that use native code in some way. Whether actually writing "Bindings" to some native library, or just consuming such a library to achieve some task, you probably will have encountered one of the following.

 * JNI. The oldest, and built into Java. Writing for JNI is rather involved. You must write glue code in both Java and the native language (probably C). There are also deployment issues, you will need to provide binaries of this native glue code for all the operating systems you support. 
 * JNA. Really made made things better, at least for developers. Here was a way to interface to native code using just Java. Deployment was also easier, if you were interfacing to pre-exisiting libraries, you just need to ship your Java code. Over time, many JNA based libraries have become available. JNA is not perfect though, the main issue being performance. While big improvements have been made with this mature library, its very nature make it unsuitable for high performance requirements. A primary reason for this is it's use of reflect.
 * JNR/FFI. Based on libffi, and in many ways similar to JNA, this avoids using reflection and so is more performant. It has other advantages too, but the biggest problem I always found was a severe lack of documentation. 
 
Other native Java related projects of note inclide BridJ - another C/C++ interop library and JNAerator, which can generate Java from C headers much like `jextract` that will be talked about in the next section.

#### Getting Started

TODO - Talk about jextract, starting with unix/libc

#### Library Loading

TODO - Talk about code copied from JNA (Native/NativeLibrary)

#### Mapping A Function

TODO - Give a couple of libc examples.

#### Mapping A Structure

TODO - Give a couple of examples.

#### Moving On To Windows

TODO - Talk about wide strings