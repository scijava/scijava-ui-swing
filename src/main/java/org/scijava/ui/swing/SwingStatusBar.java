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

package org.scijava.ui.swing;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.BevelBorder;

import org.scijava.Context;
import org.scijava.app.AppService;
import org.scijava.app.StatusService;
import org.scijava.app.event.StatusEvent;
import org.scijava.event.EventHandler;
import org.scijava.options.OptionsService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.DialogPrompt.MessageType;
import org.scijava.ui.StatusBar;
import org.scijava.ui.UIService;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.swing.task.SwingTaskMonitorComponent;

import java.awt.BorderLayout;
import java.awt.Dimension;

/**
 * Swing implementation of {@link StatusBar}.
 * 
 * @author Curtis Rueden
 */
public class SwingStatusBar extends JPanel implements StatusBar {

	private final JLabel statusText;
	private final JProgressBar progressBar;

	@Parameter
	private OptionsService optionsService;

	@Parameter
	private StatusService statusService;

	@Parameter
	private AppService appService;

	@Parameter
	private UIService uiService;

	public SwingStatusBar(final Context context) {
		context.inject(this);
		statusText = new JLabel(appService.getApp().getInfo(false));
		statusText.setBorder(new BevelBorder(BevelBorder.LOWERED));
		progressBar = new JProgressBar();
		progressBar.setVisible(false);
		setLayout(new BorderLayout());
		add(statusText, BorderLayout.CENTER);
		add(progressBar, BorderLayout.WEST);
		JComponent progress = new SwingTaskMonitorComponent(context, true, true,300, false).getComponent();
		int h = getPreferredSize().height;
		progress.setPreferredSize(new Dimension(h,h));
		add(progress, BorderLayout.EAST);
	}

	// -- SwingStatusBar methods --

	public void addEventDispatcher(final AWTInputEventDispatcher dispatcher) {
		dispatcher.register(this, false, true);
	}

	// -- StatusBar methods --

	@Override
	public void setStatus(final String message) {
		if (message == null) return; // no change
		final String text;
		if (message.isEmpty()) text = " ";
		else text = message;
		statusText.setText(text);
	}

	@Override
	public void setProgress(final int val, final int max) {
		if (max < 0) {
			progressBar.setVisible(false);
			return;
		}

		if (val >= 0 && val < max) {
			progressBar.setValue(val);
			progressBar.setMaximum(max);
			progressBar.setVisible(true);
		}
		else {
			progressBar.setVisible(false);
		}
	}

	// -- Event handlers --

	@EventHandler
	protected void onEvent(final StatusEvent event) {
		if (event.isWarning()) {
			// report warning messages to the user in a dialog box
			final String message = event.getStatusMessage();
			if (message != null && !message.isEmpty()) {
				uiService.showDialog(message, MessageType.WARNING_MESSAGE);
			}
		}
		else {
			// report status updates in the status bar
			final int val = event.getProgressValue();
			final int max = event.getProgressMaximum();
			final String message = uiService.getStatusMessage(event);
			setStatus(message);
			setProgress(val, max);
		}
	}

}
