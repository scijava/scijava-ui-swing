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

package org.scijava.ui.swing.mdi;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.scijava.display.Display;
import org.scijava.display.event.window.WinActivatedEvent;
import org.scijava.display.event.window.WinClosedEvent;
import org.scijava.display.event.window.WinClosingEvent;
import org.scijava.display.event.window.WinDeactivatedEvent;
import org.scijava.display.event.window.WinDeiconifiedEvent;
import org.scijava.display.event.window.WinIconifiedEvent;
import org.scijava.display.event.window.WinOpenedEvent;
import org.scijava.event.EventService;

/**
 * Rebroadcasts AWT internal frame events as
 * {@link org.scijava.event.SciJavaEvent}s.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class InternalFrameEventDispatcher implements InternalFrameListener {

	private final Display<?> display;
	private final EventService eventService;

	/** Creates an AWT event dispatcher for the given display. */
	public InternalFrameEventDispatcher(final Display<?> display) {
		this.display = display;
		eventService = display.getContext().getService(EventService.class);
	}

	// -- InternalFrameListener methods --

	@Override
	public void internalFrameActivated(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinActivatedEvent(display, window));
	}

	@Override
	public void internalFrameClosed(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinClosedEvent(display, window));
	}

	@Override
	public void internalFrameClosing(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinClosingEvent(display, window));
	}

	@Override
	public void internalFrameDeactivated(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinDeactivatedEvent(display, window));
	}

	@Override
	public void internalFrameDeiconified(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinDeiconifiedEvent(display, window));
	}

	@Override
	public void internalFrameIconified(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinIconifiedEvent(display, window));
	}

	@Override
	public void internalFrameOpened(final InternalFrameEvent e) {
		final JInternalFrame window = e.getInternalFrame();
		eventService.publish(new WinOpenedEvent(display, window));
	}

}
