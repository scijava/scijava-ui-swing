/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2024 SciJava developers.
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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.scijava.log.CallingClassUtils;
import org.scijava.log.IgnoreAsCallingClass;
import org.scijava.log.LogListener;
import org.scijava.log.LogMessage;

/**
 * {@link LogRecorder} is used to decouple the GUI displaying log messages from
 * the potentially highly concurrent code emitting log messages.
 * <p>
 * The recorded {@link LogMessage}s are stored in a list. New messages can only
 * be added to the end of the list. The iterators never fail and are always
 * updated.
 * </p>
 *
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public class LogRecorder implements LogListener, Iterable<LogMessage> {

	private ConcurrentExpandableList<LogMessage> recorded =
		new ConcurrentExpandableList<>();

	private List<Runnable> observers = new CopyOnWriteArrayList<>();

	private boolean recordCallingClass = false;

	/**
	 * The {@link Runnable} observer will be executed, after every new log message
	 * or text recorded. The code executed by the {@link Runnable} must by highly
	 * thread safe and must not use any kind of locks.
	 */
	public void addObservers(Runnable observer) {
		observers.add(observer);
	}

	public void removeObserver(Runnable observer) {
		observers.remove(observer);
	}

	/**
	 * The returned Iterator never fails, and will always be updated. Even if an
	 * message is added after the iterator reached the end of the list,
	 * {@link Iterator#hasNext()} will return true again, and
	 * {@link Iterator#next()} will return the new log messages element.
	 */
	@Override
	public Iterator<LogMessage> iterator() {
		return recorded.iterator();
	}

	public Stream<LogMessage> stream() {
		return recorded.stream();
	}

	/**
	 * Same as {@link #iterator()}, but the Iterator will only return log messages
	 * and text recorded after the iterator has been created.
	 */
	public Iterator<LogMessage> iteratorAtEnd() {
		return recorded.iteratorAtEnd();
	}

	public void clear() {
		recorded.clear();
	}

	public boolean isRecordCallingClass() {
		return recordCallingClass;
	}

	public void setRecordCallingClass(boolean enable) {
		this.recordCallingClass = enable;
	}

	// -- LogListener methods --

	@Override
	public void messageLogged(LogMessage message) {
		if (recordCallingClass) message.attach(CallingClassUtils.getCallingClass());
		recorded.add(message);
		notifyListeners();
	}

	// -- Helper methods --

	private void notifyListeners() {
		for (Runnable listener : observers)
			listener.run();
	}

	/**
	 * This Container manages a list of items. Items can only be added to end of
	 * the list. It's possible to add items, while iterating over the list.
	 * Iterators never fail, and they will always be updated. Even if an element
	 * is added after an iterator reached the end of the list,
	 * {@link Iterator#hasNext()} will return true again, and
	 * {@link Iterator#next()} will return the newly added element. This Container
	 * is fully thread safe.
	 *
	 * @author Matthias Arzt
	 */
	private class ConcurrentExpandableList<T> implements Iterable<T> {

		private final AtomicLong lastKey = new AtomicLong(0);

		private long firstKey = 0;

		private final Map<Long, T> map = new ConcurrentHashMap<>();

		public Stream<T> stream() {
			Spliterator<T> spliterator = Spliterators.spliteratorUnknownSize(
					iterator(), Spliterator.ORDERED);
			return StreamSupport.stream(spliterator, /* parallel */ false);
		}

		@Override
		public Iterator<T> iterator() {
			return new MyIterator(firstKey);
		}

		public Iterator<T> iteratorAtEnd() {
			return new MyIterator(lastKey.get());
		}

		public long add(T value) {
			long key = lastKey.getAndIncrement();
			map.put(key, value);
			return key;
		}

		public void clear() {
			map.clear();
			firstKey = lastKey.get();
		}

		private class MyIterator implements Iterator<T> {

			private long nextIndex;

			public MyIterator(long nextIndex) {
				this.nextIndex = nextIndex;
			}

			@Override
			public boolean hasNext() {
				return map.containsKey(nextIndex);
			}

			@Override
			public T next() {
				T value = map.get(nextIndex);
				if(value == null)
					throw new NoSuchElementException();
				nextIndex++;
				return value;
			}
		}
	}

}
