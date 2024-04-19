/*
 * JPty - A small PTY interface for Java.
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.pty4j.unix.freebsd;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;

import com.pty4j.unix.LibC;
import com.pty4j.unix.LibUtil;
import com.pty4j.unix.PtyHelpers;
import com.pty4j.unix.PtyHelpers.FDSet;

/**
 * Provides a {@link PtyHelpers.OSFacade} implementation for FreeBSD.
 */
public class OSFacadeImpl implements PtyHelpers.OSFacade {
  
  // INNER TYPES

  private final static class FreeBSD_Clib extends LibC {
    
	private static int posix_openpt(int oflag) {
	  try {
        return (int) LibCHelper.downcallHandle("posix_openpt", FunctionDescriptor.of(JAVA_INT, JAVA_INT)).invokeExact(oflag);
      } catch (Throwable ex$) {
        throw new AssertionError("should not reach here", ex$);
      }
    }
  }
  

  // CONSTUCTORS

  /**
   * Creates a new {@link OSFacadeImpl} instance.
   */
  public OSFacadeImpl() {
    PtyHelpers.ONLCR = 0x02;

    PtyHelpers.VERASE = 3;
    PtyHelpers.VWERASE = 4;
    PtyHelpers.VKILL = 5;
    PtyHelpers.VREPRINT = 6;
    PtyHelpers.VINTR = 8;
    PtyHelpers.VQUIT = 9;
    PtyHelpers.VSUSP = 10;

    PtyHelpers.ECHOKE = 0x01;
    PtyHelpers.ECHOCTL = 0x40;
  }

  // METHODS

  @Override
  public int getpt() {
    return FreeBSD_Clib.posix_openpt(LibC.O_RDWR | LibC.O_NOCTTY);
  }

  @Override
  public int login_tty(int fd) {
    return LibUtil.login_tty(fd);
  }
}
