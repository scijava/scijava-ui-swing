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
import java.awt.Font;
import java.util.function.Predicate;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * {@link TextFilterField} provides a {@link JTextField} and derives a text
 * filter from the content.
 *
 * @author Matthias Arzt
 */
class TextFilterField {

	private final JTextField textField = new JTextField();

	private JLabel prompt = new JLabel();

	private Predicate<String> filter = null;

	private Runnable changeListener = null;

	// -- constructor --

	TextFilterField(String textForPrompt) {
		initPrompt(textForPrompt);
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent documentEvent) {
				onUserInputChanged();
			}

			@Override
			public void removeUpdate(DocumentEvent documentEvent) {
				onUserInputChanged();
			}

			@Override
			public void changedUpdate(DocumentEvent documentEvent) {
				onUserInputChanged();
			}
		});
	}

	// -- TextFilterField methods --

	public JTextField getComponent() {
		return textField;
	}

	public void setChangeListener(Runnable changeListener) {
		this.changeListener = changeListener;
	}

	public Predicate<String> getFilter() {
		if (filter == null) filter = calculateFilter();
		return filter;
	}

	// -- Helper methods --

	private Predicate<String> calculateFilter() {
		String text = textField.getText();
		final String[] words = text.split(" ");
		return s -> {
			for (String word : words)
				if (!s.contains(word)) return false;
			return true;
		};
	}

	private void onUserInputChanged() {
		filter = null;
		updatePromptVisibility();
		notifyChangeListener();
	}

	private void notifyChangeListener() {
		if (changeListener != null) changeListener.run();
	}

	private void initPrompt(String text) {
		prompt.setText(text);
		prompt.setFont(textField.getFont().deriveFont(Font.ITALIC));
		prompt.setForeground(changeAlpha(textField.getForeground(), 128));
		prompt.setBorder(new EmptyBorder(textField.getInsets()));
		prompt.setHorizontalAlignment(SwingConstants.LEADING);
		textField.setLayout(new BorderLayout());
		textField.add(prompt);
		updatePromptVisibility();
	}

	private static Color changeAlpha(Color color, int alpha) {
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
	}

	private void updatePromptVisibility() {
		prompt.setVisible(textField.getDocument().getLength() == 0);
	}
}
