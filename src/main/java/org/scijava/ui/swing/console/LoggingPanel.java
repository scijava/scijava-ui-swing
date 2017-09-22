/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2017 Board of Regents of the University of
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

import java.awt.*;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import org.scijava.console.OutputListener;
import org.scijava.log.IgnoreAsCallingClass;
import org.scijava.log.LogListener;
import org.scijava.log.LogMessage;
import org.scijava.log.LogService;
import org.scijava.log.Logger;
import org.scijava.thread.ThreadService;
import org.scijava.ui.swing.StaticSwingUtils;

/**
 * LoggingPanel can display log message and console output as a list, and
 * provides convenient ways for the user to filter this list.
 * LoggingPanel implements {@link LogListener} and {@link OutputListener}, that
 * way it can receive log message and console output from {@link LogService},
 * {@link Logger} and {@link org.scijava.console.ConsoleService}
 *
 * @see LogService
 * @see Logger
 * @see org.scijava.console.ConsoleService
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public class LoggingPanel extends JPanel implements LogListener
{
	private JTextPane textPane;
	private JScrollPane scrollPane;

	private StyledDocument doc;
	private Style defaultStyle;

	private final LogFormatter formatter = new LogFormatter();

	private final ThreadService threadService;

	public LoggingPanel(ThreadService threadService) {
		this.threadService = threadService;
		initGui();
	}

	public void clear() {
		textPane.setText("");
	}

	// -- LogListener methods --

	@Override
	public void messageLogged(LogMessage message) {
		appendText(formatter.format(message), defaultStyle);
	}

	// -- Helper methods --

	private void appendText(final String text, final Style style) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				final boolean atBottom =
						StaticSwingUtils.isScrolledToBottom(scrollPane);
				try {
					doc.insertString(doc.getLength(), text, style);
				}
				catch (final BadLocationException exc) {
					throw new RuntimeException(exc);
				}
				if (atBottom) StaticSwingUtils.scrollToBottom(scrollPane);
			}
		});
	}

	private synchronized void initGui() {
		setLayout(new MigLayout("inset 0", "[grow,fill]", "[grow,fill,align top]"));

		textPane = new JTextPane();
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		textPane.setEditable(false);

		doc = textPane.getStyledDocument();

		defaultStyle = createStyle("stdoutLocal", null, Color.black, null, null);

		// NB: We wrap the JTextPane in a JPanel to disable
		// the text pane's intelligent line wrapping behavior.
		// I.e.: we want console lines _not_ to wrap, but instead
		// for the scroll pane to show a horizontal scroll bar.
		// Thanks to: https://tips4java.wordpress.com/2009/01/25/no-wrap-text-pane/
		final JPanel textPanel = new JPanel();
		textPanel.setLayout(new BorderLayout());
		textPanel.add(textPane);

		scrollPane = new JScrollPane(textPanel);
		scrollPane.setPreferredSize(new Dimension(600, 600));

		// Make the scroll bars move at a reasonable pace.
		final FontMetrics fm = scrollPane.getFontMetrics(scrollPane.getFont());
		final int charWidth = fm.charWidth('a');
		final int lineHeight = fm.getHeight();
		scrollPane.getHorizontalScrollBar().setUnitIncrement(charWidth);
		scrollPane.getVerticalScrollBar().setUnitIncrement(2 * lineHeight);

		add(scrollPane);
	}

	private Style createStyle(final String name, final Style parent,
							  final Color foreground, final Boolean bold, final Boolean italic)
	{
		final Style style = textPane.addStyle(name, parent);
		if (foreground != null) StyleConstants.setForeground(style, foreground);
		if (bold != null) StyleConstants.setBold(style, bold);
		if (italic != null) StyleConstants.setItalic(style, italic);
		return style;
	}

	// -- Helper methods - testing --

	JTextPane getTextPane() {
		return textPane;
	}
}
