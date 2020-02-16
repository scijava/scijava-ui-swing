/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2020 SciJava developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.ui.swing.console;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.scijava.log.LogLevel;
import org.scijava.log.LogMessage;
import org.scijava.log.LogSource;

/**
 * Tests {@link LogRecorderTest}.
 *
 * @author Matthias Arzt
 */
public class LogRecorderTest {

	private LogRecorder recorder;
	private MyListener listener;

	@Before
	public void setup() {
		recorder = new LogRecorder();
		listener = new MyListener(recorder);
	}

	@Test
	public void testLogMessageDelivery() {
		recorder.clear();
		LogMessage message = newLogMessage();
		recorder.messageLogged(message);
		assertEquals(Arrays.asList(message), listener.messages());
	}

	private LogMessage newLogMessage() {
		return new LogMessage(LogSource.newRoot(), LogLevel.INFO, "Hello World!");
	}

	@Test
	public void testReadOldLog() {
		LogRecorder recorder = new LogRecorder();
		LogMessage message = newLogMessage();
		recorder.messageLogged(message);
		assertEquals(Arrays.asList(message), recorder.stream().collect(Collectors
			.toList()));
	}

	@Test
	public void testUpdatedReading() {
		// setup
		LogRecorder recorder = new LogRecorder();
		LogMessage msgA = newLogMessage();
		LogMessage msgB = newLogMessage();
		Iterator<LogMessage> iterator = recorder.iterator();
		// process & test
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgA);
		assertTrue(iterator.hasNext());
		assertSame(msgA, iterator.next());
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgB);
		assertTrue(iterator.hasNext());
		assertSame(msgB, iterator.next());
	}

	@Test
	public void testUpdatedStream() {
		// setup
		LogRecorder recorder = new LogRecorder();
		LogMessage msgA = newLogMessage();
		LogMessage msgB = newLogMessage();
		Iterator<LogMessage> iterator = recorder.stream().iterator();
		// process & test
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgA);
		assertTrue(iterator.hasNext());
		assertEquals(msgA, iterator.next());
		assertFalse(iterator.hasNext());
		recorder.messageLogged(msgB);
		assertTrue(iterator.hasNext());
		assertEquals(msgB, iterator.next());
	}

	@Test
	public void testRecordCallingClass() {
		listener.clear();
		recorder.setRecordCallingClass(true);
		recorder.messageLogged(newLogMessage());
		recorder.setRecordCallingClass(false);
		LogMessage message = listener.messages().get(0);
		assertTrue(message.attachments().contains(this.getClass()));
	}

	@Test
	public void testIsRecordCallingClass() {
		recorder.setRecordCallingClass(true);
		assertTrue(recorder.isRecordCallingClass());
		recorder.setRecordCallingClass(false);
		assertFalse(recorder.isRecordCallingClass());
	}

	@Test
	public void testIteratorAtEnd() {
		LogMessage messageA = newLogMessage();
		recorder.messageLogged(messageA);
		Iterator<LogMessage> iterator = recorder.iteratorAtEnd();
		LogMessage messageB = newLogMessage();
		recorder.messageLogged(messageB);
		assertEquals(messageB, iterator.next());
	}

	@Test
	public void testClear() {
		LogRecorder recorder = new LogRecorder();
		LogMessage messageA = newLogMessage();
		recorder.messageLogged(messageA);
		recorder.clear();
		Iterator<LogMessage> iterator = recorder.iterator();
		LogMessage messageB = newLogMessage();
		recorder.messageLogged(messageB);
		assertTrue(iterator.hasNext());
		assertEquals(messageB, iterator.next());
	}

	private static class MyListener implements Runnable {

		private final Iterator<LogMessage> iterator;

		private List<LogMessage> messages = new LinkedList<>();

		private MyListener(LogRecorder recorder) {
			this.iterator = recorder.iterator();
			recorder.addObservers(this);
		}

		@Override
		public void run() {
			while (iterator.hasNext()) {
				messages.add(iterator.next());
			}
		}

		public void clear() {
			messages.clear();
		}

		public List<LogMessage> messages() {
			return messages;
		}
	}
}
