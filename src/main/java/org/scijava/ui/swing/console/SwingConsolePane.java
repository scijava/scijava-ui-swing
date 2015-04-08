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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import org.scijava.Context;
import org.scijava.console.OutputEvent;
import org.scijava.console.OutputEvent.Source;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.UIService;
import org.scijava.ui.console.AbstractConsolePane;
import org.scijava.ui.console.ConsolePane;

/**
 * Swing implementation of {@link ConsolePane}.
 *
 * @author Curtis Rueden
 */
public class SwingConsolePane extends AbstractConsolePane<JPanel> {

	@Parameter
	private ThreadService threadService;

	private JPanel consolePanel;
	private JScrollPane scrPane;
	private JTextPane textPane;
	private StyledDocument doc;
	private Style stdoutLocal;
	private Style stderrLocal;
	private Style stdoutGlobal;
	private Style stderrGlobal;

	/**
	 * The console pane's containing window; e.g., a {@link javax.swing.JFrame} or
	 * {@link javax.swing.JInternalFrame}.
	 */
	private Component window;

	public SwingConsolePane(final Context context) {
		super(context);
	}

	// -- SwingConsolePane methods --

	/** Sets the window which should be shown when {@link #show()} is called. */
	public void setWindow(final Component window) {
		this.window = window;
	}

	// -- ConsolePane methods --

	@Override
	public void append(final OutputEvent event) {
		if (consolePanel == null) initConsolePanel();
		try {
			doc.insertString(doc.getLength(), event.getOutput(), getStyle(event));
		}
		catch (final BadLocationException exc) {
			throw new RuntimeException(exc);
		}
		JScrollBar sb = scrPane.getVerticalScrollBar();
		sb.setValue(sb.getMaximum());
	}

	@Override
	public void show() {
		if (window == null) return;
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				window.setVisible(true);
			}
		});
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		if (consolePanel == null) initConsolePanel();
		return consolePanel;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

	// -- Helper methods - lazy initialization --

	private synchronized void initConsolePanel() {
		if (consolePanel != null) return;

		final JPanel panel = new JPanel();
		panel.setLayout(new MigLayout("", "[grow,fill]", "[grow,fill,align top]"));

		textPane = new JTextPane();
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textPane.setEditable(false);

		doc = textPane.getStyledDocument();

		stdoutLocal = createStyle("stdoutLocal", null, Color.black, null, null);
		stderrLocal = createStyle("stderrLocal", null, Color.red, null, null);
		stdoutGlobal = createStyle("stdoutGlobal", stdoutLocal, null, null, true);
		stderrGlobal = createStyle("stderrGlobal", stderrLocal, null, null, true);

		// NB: We wrap the JTextPane in a JPanel to disable
		// the text pane's intelligent line wrapping behavior.
		// I.e.: we want console lines _not_ to wrap, but instead
		// for the scroll pane to show a horizontal scroll bar.
		// Thanks to: https://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
		final JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.add(textPane);

		final JScrollPane scrollPane = new JScrollPane(textPanel);
		scrollPane.setPreferredSize(new Dimension(600, 600));
		panel.add(scrollPane);

		scrPane = scrollPane;
		consolePanel = panel;
	}

	// -- Helper methods --

	private Style createStyle(final String name, final Style parent,
		final Color foreground, final Boolean bold, final Boolean italic)
	{
		final Style style = textPane.addStyle(name, parent);
		if (foreground != null) StyleConstants.setForeground(style, foreground);
		if (bold != null) StyleConstants.setBold(style, bold);
		if (italic != null) StyleConstants.setItalic(style, italic);
		return style;
	}

	private Style getStyle(final OutputEvent event) {
		final boolean stderr = event.getSource() == Source.STDERR;
		final boolean contextual = event.isContextual();
		if (stderr) return contextual ? stderrLocal : stderrGlobal;
		return contextual ? stdoutLocal : stdoutGlobal;
	}

	// -- Main method --

	/** A manual test drive of the Swing UI's console pane. */
	public static void main(final String[] args) throws Exception {
		final Context context = new Context();
		context.service(UIService.class).showUI();

		System.out.println("Hello!");

		final ThreadService threadService = context.service(ThreadService.class);
		threadService.run(new Runnable() {

			@Override
			public void run() {
				System.out.println("This is a test of the emergency console system.");
				System.err.println("In a real emergency, your computer would explode.");
			}

		}).get();

		System.err.println("Goodbye!");
	}

}
