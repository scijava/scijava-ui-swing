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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import org.scijava.Priority;
import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.Desktop;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.DialogPrompt.OptionType;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.ui.awt.AWTDropTargetEventDispatcher;
import org.scijava.ui.swing.AbstractSwingUI;
import org.scijava.ui.swing.SwingApplicationFrame;
import org.scijava.ui.swing.console.SwingConsolePane;
import org.scijava.ui.swing.mdi.viewer.SwingMdiDisplayWindow;

/**
 * Swing-based MDI user interface.
 * 
 * @author Grant Harris
 * @author Curtis Rueden
 */
@Plugin(type = UserInterface.class, name = SwingMdiUI.NAME,
	priority = Priority.LOW)
public class SwingMdiUI extends AbstractSwingUI {

	public static final String NAME = "swing-mdi";

	@Parameter
	private EventService eventService;

	@Parameter
	private UIService uiService;

	private JMDIDesktopPane desktopPane;

	private JScrollPane scrollPane;

	// -- UserInterface methods --

	@Override
	public Desktop getDesktop() {
		return desktopPane;
	}

	@Override
	public SwingMdiDisplayWindow createDisplayWindow(Display<?> display) {
		final SwingMdiDisplayWindow displayWindow = new SwingMdiDisplayWindow();

		// broadcast internal frame events
		displayWindow
			.addEventDispatcher(new InternalFrameEventDispatcher(display));

		// broadcast drag-and-drop events
		new AWTDropTargetEventDispatcher(display, eventService);

		return displayWindow;
	}

	@Override
	public SwingMdiDialogPrompt dialogPrompt(final String message,
		final String title, final MessageType msg, final OptionType option)
	{
		final UserInterface ui = uiService.getDefaultUI();
		return new SwingMdiDialogPrompt(ui, message, title, msg, option);
	}

	// -- Internal methods --

	@Override
	protected void setupAppFrame() {
		final SwingApplicationFrame appFrame = getApplicationFrame();
		desktopPane = new JMDIDesktopPane();
		// TODO desktopPane.setTransferHandler(new DropFileTransferHandler());
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(desktopPane);
		desktopPane.setBackground(new Color(200, 200, 255));
		appFrame.getContentPane().add(scrollPane);
		appFrame.setBounds(getWorkSpaceBounds());
	}

	@Override
	protected void setupConsole() {
		final SwingConsolePane cPane = getConsolePane();
		if (cPane == null) return;
		final JInternalFrame frame = new JInternalFrame("Console");
		desktopPane.add(frame);
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame.setContentPane(cPane.getComponent());
		frame.setJMenuBar(createConsoleMenu());
		frame.pack();
		cPane.setWindow(frame);
	}

	// -- Helper methods --

	private Rectangle getWorkSpaceBounds() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
	}

}
