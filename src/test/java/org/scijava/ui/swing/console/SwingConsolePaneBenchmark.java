/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
 * Wisconsin-Madison.
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

import java.util.concurrent.Future;

import org.scijava.Context;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;

/**
 * A behavioral test and benchmark of {@link SwingConsolePane}.
 *
 * @author Curtis Rueden
 */
public class SwingConsolePaneBenchmark {

	// -- Main method --

	/** A manual test drive of the Swing UI's console pane. */
	public static void main(final String[] args) throws Exception {
		final Context context = new Context();
		context.service(UIService.class).showUI();

		System.out.print("Hello ");
		System.err.println("world!");

		final int numThreads = 50;
		final int numOperations = 20;

		final String[] streamLabels =
			{ ": {ERR} iteration #", ": {OUT} iteration #" };
		final String outLabel = streamLabels[1];
		final String errLabel = streamLabels[0];

		final int initialDelay = 500;

		Thread.sleep(initialDelay);

		final long start = System.currentTimeMillis();

		// emit a bunch of output on multiple threads concurrently
		final ThreadService threadService = context.service(ThreadService.class);
		final Future<?>[] f = new Future<?>[numThreads];
		for (int t = 0; t < numThreads; t++) {
			final int tNo = t;
			f[t] = threadService.run(new Runnable() {

				@Override
				public void run() {
					for (int i = 0; i < numOperations; i++) {
						System.out.print(str(tNo, outLabel, i) + "\n");
						System.err.print(str(tNo, errLabel, i) + "\n");
					}
				}
			});
		}

		// wait for all output threads to finish
		for (int t = 0; t < numThreads; t++) {
			f[t].get();
		}

		System.err.print("Goodbye ");
		System.out.println("cruel world!");

		final long end = System.currentTimeMillis();
		System.out.println();
		System.out.println("Benchmark took " + (end - start) + " ms");
	}

	// - Helper methods --

	private static String str(final int t, final String separator, final int i) {
		return pad(t) + separator + pad(i);
	}

	private static String pad(final int n) {
		return n < 10 ? "0" + n : "" + n;
	}

}
