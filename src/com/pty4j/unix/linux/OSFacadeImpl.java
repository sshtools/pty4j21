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
package com.pty4j.unix.linux;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;
import java.lang.foreign.ValueLayout;

import com.pty4j.unix.LibC;
import com.pty4j.unix.LibUtil;
import com.pty4j.unix.PtyHelpers;

/**
 * Provides a {@link com.pty4j.unix.PtyHelpers.OSFacade} implementation for Linux.
 */
public class OSFacadeImpl implements PtyHelpers.OSFacade {
  
  private final static int NFBBITS = (int)ValueLayout.JAVA_LONG.byteSize() * 8;
  private final static int FD_COUNT = 1024;
  private final static SequenceLayout FD_ARRAY_LAYOUT = MemoryLayout.sequenceLayout((FD_COUNT + NFBBITS - 1) / NFBBITS, ValueLayout.JAVA_LONG); /* TODO const */
	 
  // CONSTUCTORS

  /**
   * Creates a new {@link OSFacadeImpl} instance.
   */
  public OSFacadeImpl() {
    PtyHelpers.ONLCR = 0x04;

    PtyHelpers.VINTR = 0;
    PtyHelpers.VQUIT = 1;
    PtyHelpers.VERASE = 2;
    PtyHelpers.VKILL = 3;
    PtyHelpers.VSUSP = 10;
    PtyHelpers.VREPRINT = 12;
    PtyHelpers.VWERASE = 14;

    PtyHelpers.ECHOKE = 0x01;
    PtyHelpers.ECHOCTL = 0x40;
  }

  // METHODS

  @Override
  public int getpt() {
    try (var offHeap = Arena.ofConfined()) {
	  return LibC.open(Arena.global().allocateFrom("/dev/ptmx"), LibC.O_RDWR | LibC.O_NOCTTY);
    }
  }

  @Override
  public int login_tty(int fd) {
    return LibUtil.login_tty(fd);
  }

}
