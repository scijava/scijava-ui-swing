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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import org.scijava.console.OutputEvent;
import org.scijava.console.OutputListener;
import org.scijava.log.IgnoreAsCallingClass;
import org.scijava.thread.ThreadService;
import org.scijava.ui.swing.StaticSwingUtils;

/**
 * {@link ConsolePanel} is a {@link JPanel} holding a {@link JTextArea}. It can
 * be used to display text written to System.out and System.err. Therefor it can
 * be added as {@link OutputListener} to
 * {@link org.scijava.console.ConsoleService}.
 *
 * @author Matthias Arzt
 */
@IgnoreAsCallingClass
public class ConsolePanel extends JPanel implements OutputListener
{
	private JTextPane textPane;
	private JScrollPane scrollPane;

	private StyledDocument doc;
	private Style stdoutLocal;
	private Style stderrLocal;
	private Style stdoutGlobal;
	private Style stderrGlobal;

	private final ThreadService threadService;

	public ConsolePanel(ThreadService threadService) {
		this.threadService = threadService;
		initGui();
	}

	public void clear() {
		textPane.setText("");
	}

	@Override
	public void outputOccurred(OutputEvent event) {
		threadService.queue(new Runnable() {

			@Override
			public void run() {
				final boolean atBottom =
						StaticSwingUtils.isScrolledToBottom(scrollPane);
				try {
					doc.insertString(doc.getLength(), event.getOutput(), getStyle(event));
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
		final boolean stderr = event.getSource() == OutputEvent.Source.STDERR;
		final boolean contextual = event.isContextual();
		if (stderr) return contextual ? stderrLocal : stderrGlobal;
		return contextual ? stdoutLocal : stdoutGlobal;
	}

	public JTextPane getTextPane() {
		return textPane;
	}
}
