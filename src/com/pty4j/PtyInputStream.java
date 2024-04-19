package com.pty4j;

import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.lang.foreign.MemorySegment;
import java.nio.ByteBuffer;

public abstract class PtyInputStream extends InputStream {
	
	private boolean warned;

	/**
	 * Platform specific streams may implement this to provide a fast path to the
	 * data coming back from the pty.
	 * 
	 * @param segment segment
	 * @param len length to read
	 * @return length read
	 * @throws IOException on error
	 */
	public int fastRead(MemorySegment segment, int len) throws IOException {
		/* Default implementation, will in fact be slower */
		if(!warned) {
			System.getLogger(PtyInputStream.class.getName()).log(Level.WARNING, "{0} does not implement as concrete fastRead() method, so a fallback is in use. This will likely be slower than a standard read()", getClass().getName());
			warned = true;
		}
		
		ByteBuffer buf = segment.asByteBuffer();
		byte[] bytes = new byte[len];
		int read = read(bytes, 0, len);
		if(read != -1) {
			buf.put(bytes, 0, read);
		}
		return read;
		
	}
}
