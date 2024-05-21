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

package org.scijava.ui.swing.commands;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import org.scijava.command.Command;
import org.scijava.event.EventDetails;
import org.scijava.event.EventHistory;
import org.scijava.event.EventHistoryListener;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 * Listens for events, displaying results in a text window.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Command.class, menuPath = "Plugins>Debug>Watch Events")
public class WatchEvents implements Command, EventHistoryListener {

	// -- Parameters --

	@Parameter
	private EventHistory eventHistory;

	@Parameter
	private LogService log;

	// -- Fields --

	private WatchEventsFrame watchEventsFrame;

	// -- Runnable methods --

	@Override
	public void run() {
		watchEventsFrame = new WatchEventsFrame(eventHistory, log);

		// update UI when event history changes
		eventHistory.addListener(this);

		// stop listening for history changes when the UI goes away
		watchEventsFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent e) {
				eventHistory.removeListener(WatchEvents.this);
			}
		});

		watchEventsFrame.setVisible(true);
	}

	// -- EventHistoryListener methods --

	@Override
	public void eventOccurred(final EventDetails details) {
		watchEventsFrame.append(details);
	}

}
