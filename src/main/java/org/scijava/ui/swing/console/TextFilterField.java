/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2026 SciJava developers.
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

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * {@link TextFilterField} provides a {@link JTextField} and derives a text
 * filter from the content. The filter accepts text containing all the
 * (space separated) words typed by the user, ignoring case.
 *
 * @author Matthias Arzt
 */
class TextFilterField {

	private final JTextField textField = new JTextField();

	private volatile Predicate<String> filter = null;

	private Runnable changeListener = null;

	// -- constructor --

	TextFilterField(String textForPrompt) {
		textField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, textForPrompt.trim());
		textField.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
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
		final String[] words = Arrays.stream(textField.getText().toLowerCase(
			Locale.ROOT).split("\\s+")).filter(w -> !w.isEmpty()).toArray(
				String[]::new);
		if (words.length == 0) return s -> true;
		return s -> {
			final String lower = s.toLowerCase(Locale.ROOT);
			for (final String word : words)
				if (!lower.contains(word)) return false;
			return true;
		};
	}

	private void onUserInputChanged() {
		filter = null;
		notifyChangeListener();
	}

	private void notifyChangeListener() {
		if (changeListener != null) changeListener.run();
	}
}
