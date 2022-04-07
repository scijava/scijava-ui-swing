/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2022 SciJava developers.
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

package org.scijava.ui.swing.sdi;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.UserInterface;
import org.scijava.ui.awt.AWTDropTargetEventDispatcher;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.awt.AWTWindowEventDispatcher;
import org.scijava.ui.swing.AbstractSwingUI;
import org.scijava.ui.swing.SwingApplicationFrame;
import org.scijava.ui.swing.SwingUI;
import org.scijava.ui.swing.console.SwingConsolePane;
import org.scijava.ui.swing.viewer.SwingDisplayWindow;

/**
 * Swing-based MDI user interface.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
@Plugin(type = UserInterface.class, name = SwingUI.NAME)
public class SwingSDIUI extends AbstractSwingUI {

	@Parameter
	private EventService eventService;

	// -- UserInterface methods --

	@Override
	public SwingDisplayWindow createDisplayWindow(final Display<?> display) {
		return createDisplayWindow(display, eventService);
	}

	@Override
	public DialogPrompt dialogPrompt(final String message, final String title,
		final MessageType msg, final OptionType option)
	{
		return new SwingDialogPrompt(message, title, msg, option);
	}

	// -- Utility methods --

	/**
	 * Utility method for creating {@link SwingDisplayWindow}s.
	 */
	public static SwingDisplayWindow createDisplayWindow(
		final Display<?> display, final EventService eventService)
	{
		final SwingDisplayWindow displayWindow = new SwingDisplayWindow();

		// broadcast input events (keyboard and mouse)
		new AWTInputEventDispatcher(display).register(displayWindow, true, false);

		// broadcast window events
		new AWTWindowEventDispatcher(display).register(displayWindow);

		// broadcast drag-and-drop events
		new AWTDropTargetEventDispatcher(display, eventService);

		return displayWindow;
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		final JPanel pane = new JPanel();
		appFrame.setContentPane(pane);
		pane.setLayout(new BorderLayout());
	}

	@Override
	protected void setupConsole() {
		final SwingConsolePane cPane = getConsolePane();
		if (cPane == null) return;
		final JFrame frame = new JFrame("Console");
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setContentPane(cPane.getComponent());
		frame.setJMenuBar(createConsoleMenu());
		frame.pack();
		cPane.setWindow(frame);
	}

}
