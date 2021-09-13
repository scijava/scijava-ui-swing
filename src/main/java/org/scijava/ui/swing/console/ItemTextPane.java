/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2021 SciJava developers.
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

import java.awt.Font;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.StyledDocument;

import org.scijava.Context;
import org.scijava.plugin.Parameter;
import org.scijava.thread.ThreadService;
import org.scijava.ui.swing.StaticSwingUtils;

/**
 * {@link ItemTextPane} provides a {@link JTextPane} in a {@link JScrollPane}.
 * The content is provided as {@link Iterator} of {@link ItemTextPane.Item}.
 * ItemTextPane is used to display a large list of items, which is often
 * replaced and extended.
 * <p>
 * Replacing the entire list requires an update to the {@link StyledDocument}.
 * This is performed in a worker thread; as a result, the event dispatch thread
 * will not be blocked.
 * <p>
 * An {@link Item} can be tagged and incomplete. If this is the case the item
 * will be removed, when the next item with the same tag is displayed.
 * <p>
 * {@link ItemTextPane} is used in {@link LoggingPanel}.
 *
 * @author Matthias Arzt
 */
class ItemTextPane {

	private DocumentCalculator initialCalculator = null;

	private JTextPane textPane = new JTextPane();

	private JScrollPane scrollPane = new JScrollPane(textPane);

	private boolean waitingForProcessNewItems = false;

	private DocumentCalculator calculator =
		new DocumentCalculator(Collections.<Item> emptyList().iterator());

	@Parameter
	private ThreadService threadService;

	// -- constructor --

	ItemTextPane(final Context context) {
		context.inject(this);
		textPane.setEditable(false);
		textPane.setFont(new Font("monospaced", Font.PLAIN, 12));
	}

	// -- ItemTextPane methods --

	JComponent getJComponent() {
		return scrollPane;
	}

	public void setPopupMenu(JPopupMenu menu) {
		textPane.setComponentPopupMenu(menu);
	}

	/**
	 * Set the {@link ItemTextPane.Item}s to be displayed in the
	 * {@link JTextPane}.
	 * 
	 * @param data The iterator will be used by a SwingWorker, and the
	 *          SwingThread. NB: Each time {@link ItemTextPane#update()} is called
	 *          {@link Iterator#hasNext()} will be called again (even if it
	 *          returned false before) to check if maybe the iterator provides new
	 *          items.
	 */
	public void setData(Iterator<Item> data) {
		calculator.cancel();
		if (initialCalculator != null) initialCalculator.cancel();
		DocumentCalculator calculator = new DocumentCalculator(data);
		initialCalculator = calculator;
		threadService.run(() -> initCalculator(calculator));
	}

	/**
	 * This initiates to check {@link Iterator#hasNext()} of iterator previously
	 * set with {@link #setData(Iterator)} again. If {@link Iterator#hasNext()}
	 * returns true, the new items will be red from the Iterator, and displayed.
	 */
	public void update() {
		if (waitingForProcessNewItems) return;
		waitingForProcessNewItems = true;
		threadService.queue(this::processNewItemsInSwingThread);
	}

	/** Copy selected text to the clipboard. */
	public void copySelectionToClipboard() {
		textPane.copy();
	}

	// -- Helper methods --

	private void processNewItemsInSwingThread() {
		if (calculator.isCanceled()) return;

		waitingForProcessNewItems = false;
		boolean atBottom = StaticSwingUtils.isScrolledToBottom(scrollPane);
		calculator.update();
		if (atBottom) StaticSwingUtils.scrollToBottom(scrollPane);
	}

	private void initCalculator(DocumentCalculator calculator) {
		calculator.update();
		if (calculator.isCanceled()) return;
		threadService.queue(() -> applyCalculator(calculator));
	}

	private void applyCalculator(DocumentCalculator calculator) {
		if (initialCalculator != calculator) return;
		this.calculator = calculator;
		textPane.setDocument(calculator.document());
		processNewItemsInSwingThread();
		threadService.queue(() -> StaticSwingUtils.scrollToBottom(scrollPane));
	}

	// -- Helper methods - testing --

	JTextPane getTextPane() {
		return textPane;
	}

	// -- public Helper classes --

	public static class Item {

		private final String text;
		private final AttributeSet style;

		Item(AttributeSet style, String text) {
			this.style = style;
			this.text = text;
		}

		final String text() {
			return text;
		}

		final AttributeSet style() {
			return style;
		}
	}

	// -- Helper classes --

	/**
	 * {@link DocumentCalculator} is used to calculate a {@link StyledDocument}
	 * for a given {@link Iterator} of {@link Item}s. NB: Items can be incomplete
	 * and tagged. Such incomplete tagged Item will be replaced by the following
	 * item with the same tag.
	 */
	static class DocumentCalculator {

		private final Iterator<Item> data;

		private final StyledDocument document = new DefaultStyledDocument();

		private boolean canceled = false;

		DocumentCalculator(Iterator<Item> data) {
			this.data = data;
		}

		public StyledDocument document() {
			return document;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void cancel() {
			canceled = true;
		}

		public synchronized void update() {
			while (data.hasNext() && !canceled)
				addText(data.next());
		}

		private void addText(Item item) {
			try {
				document.insertString(document.getLength(), item.text(), item.style());
			} catch (BadLocationException e) {
				// ignore
			}
		}
	}
}
