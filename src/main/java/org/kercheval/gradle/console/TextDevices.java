/*
 * Copyright (c) 2010 McDowell Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
 * Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions: The
 * above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.kercheval.gradle.console;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Convenience class for providing {@link TextDevice} implementations.
 * 
 * @author McDowell
 */
public final class TextDevices
{
	private static final TextDevice DEFAULT = (System.console() == null) ? streamDevice(System.in,
		System.out) : new ConsoleDevice(System.console());

	/**
	 * Returns a text I/O device wrapping the given streams.
	 * 
	 * @param reader an input source
	 * @param writer an output target
	 * @return a new device
	 */
	public static TextDevice characterDevice(final BufferedReader reader, final PrintWriter writer)
	{
		return new CharacterDevice(reader, writer);
	}

	/**
	 * The default system text I/O device.
	 * 
	 * @return the default device
	 */
	public static TextDevice defaultTextDevice()
	{
		return DEFAULT;
	}

	/**
	 * Returns a text I/O device wrapping the given streams. The default system encoding is used to decode/encode data.
	 * 
	 * @param in an input source
	 * @param out an output target
	 * @return a new device
	 */
	public static TextDevice streamDevice(final InputStream in, final OutputStream out)
	{
		final BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		final PrintWriter writer = new PrintWriter(out, true);
		return new CharacterDevice(reader, writer);
	}

	private TextDevices()
	{}
}
