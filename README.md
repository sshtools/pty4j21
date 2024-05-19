# Pty4J21 - Pseudo terminal(PTY) implementation in Java 21+.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.sshtools/pty4j21/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.sshtools/pty4j21)
[![Coverage Status](https://coveralls.io/repos/github/sshtools/pty4j21/badge.svg)](https://coveralls.io/github/sshtools/pty4j21)
[![javadoc](https://javadoc.io/badge2/com.sshtools/pty4j21/javadoc.svg)](https://javadoc.io/doc/com.sshtools/pty4j21)
![JPMS](https://img.shields.io/badge/JPMS-com.sshtools.pty4j-purple) 

![Pty4J21](banner.png) 

This is a Java implementation of PTY, forked from [Pty4J](https://github.com/JetBrains/pty4j). Written for Java 21+, and making use of the [Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/21/core/foreign-function-and-memory-api.html) together with the binary helpers and libraries from the upstream project.

While [Pty4J](https://github.com/JetBrains/pty4j) is pretty good, I feel some improvements are possible because Java has more moved on. See [More About This Fork](https://github.com/sshtools/pty4j21/wiki/More-About-This-Fork) and [How Was This Achieved](https://github.com/sshtools/pty4j21/wiki/How-Was-This-Achieved) for more on the rationale behind this and technical information. 

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

There are 2 versions of Pty4J21, because FFMAPI went from preview at Java 21 to general availability at Java 22, and 
there were some minor but incompatible API changes.

 * If you are using *Java 21 only*, then you want version `0.21.x` of Pty4J21. This version is built from the branch `pty421_java21_only`. You will also need the `--enable-preview` flag when using the library.
 * If you are using *Java 22 or higher*, then you want version `0.22.x` or Pty4J21. This version build from the branch `master`.

The releases are published to Maven Central: [com.sshtools:pty4j21](https://search.maven.org/artifact/com.sshtools/pty4j21).

### Maven

Adjust version accordinglty

```xml
<dependency>
  <groupId>com.sshtools</groupId>
  <artifactId>pty4j21</artifactId>
  <version>0.22.0-SNAPSHOT</version>
</dependency>
```

### Gradle

```groovy
dependencies {
  implementation 'com.sshtools:pty4j:0.22.0'
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
If you are not using JPMS, then `--enable-native-access=ALL-UNNAMED` will be neccessary. 

### Java 22

As from Java 22, you can remove the `--enable-preview` flag, but the `--enable-native-access` flag is required
and has same semantics as Java 21.

*Note, as of 19th May 2014 only Windows and Linux have been tested with the Java 22 variant. As soon as I am able to test and prove Mac OS too, that Java 22 version will be made stable and published to Maven central.* 
 

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

## Future Plans

Depending on interest, pty4j21 may diverge further from upstream. We are certainly going to be using it in place of pty4j for our own projects, so this fork is unlikely to become stale any time soon.

Some initial ideas of future direction include ..

 * Re-visit the helper libraries and executables to see if any can be re-implemented in Java. 
 * Investigate use with Graal native image. 
   * Provide Graal meta-data for easy native compilation.
   * Investigate whether running as a native image makes any difference to fork behaviour.
   * Investigate rewriting native libraries and helpers in Java, natively compiled.
 * Double as a serial port implementation too, i.e. like PureJavaComm. It is not far off! The Unix side has most of what is needed, it would mainly just be a Windows implementation and a public API.
 * Automatic discovery of shells available on the host operating system. 
