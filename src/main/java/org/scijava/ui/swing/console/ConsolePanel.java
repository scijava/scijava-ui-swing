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

package org.scijava.ui.swing.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import net.miginfocom.swing.MigLayout;

import org.scijava.Context;
import org.scijava.console.OutputEvent;
import org.scijava.console.OutputListener;
import org.scijava.log.IgnoreAsCallingClass;
import org.scijava.plugin.Parameter;
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

	@Parameter
	private ThreadService threadService;

	public ConsolePanel(final Context context) {
		context.inject(this);
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
		textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, textPane.getFont().getSize()));
		textPane.setEditable(false);

		doc = textPane.getStyledDocument();

		stdoutLocal = createStyle("stdoutLocal", null, defaultFontColor(), null, null);
		stderrLocal = createStyle("stderrLocal", null, Color.RED, null, null);
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

		// Make the scroll bars move at a reasonable pace.
		final FontMetrics fm = scrollPane.getFontMetrics(textPane.getFont());
		final int charWidth = fm.charWidth('a');
		final int lineHeight = fm.getHeight();
		scrollPane.setPreferredSize(new Dimension(charWidth * 80, lineHeight * 10)); //80 columns, 10 lines
		scrollPane.getHorizontalScrollBar().setUnitIncrement(charWidth);
		scrollPane.getVerticalScrollBar().setUnitIncrement(2 * lineHeight);
		textPane.setComponentPopupMenu(initMenu());
		add(scrollPane);
	}

	private JPopupMenu initMenu() {
		final JPopupMenu menu = new JPopupMenu();
		JMenuItem item = new JMenuItem("Copy");
		item.addActionListener(e -> textPane.copy());
		menu.add(item);
		item = new JMenuItem("Clear");
		item.addActionListener(e -> clear());
		menu.add(item);
		item = new JMenuItem("Select All");
		item.addActionListener(e -> textPane.selectAll());
		menu.add(item);
		return menu;
	}

	@Override
	public void updateUI() {
		if (stdoutLocal != null)
			StyleConstants.setForeground(stdoutLocal, defaultFontColor());
		super.updateUI();
	}

	// -- Helper methods --

	private static Color defaultFontColor() {
		final Color color = UIManager.getColor("TextPane.foreground");
		return (color == null) ? Color.BLACK : color;
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
