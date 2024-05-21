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

package org.scijava.ui.swing.widget;

import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

import org.scijava.log.LogService;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.TextWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of text field widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingTextWidget extends SwingInputWidget<String> implements
	DocumentListener, TextWidget<JPanel>
{

	private LogService log;

	private JTextComponent textComponent;

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public String getValue() {
		return textComponent.getText();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		log = model.getContext().getService(LogService.class);

		final int columns = model.getItem().getColumnCount();

		// construct text widget of the appropriate style, if specified
		boolean addScrollPane = false;
		if (model.isStyle(TextWidget.AREA_STYLE)) {
			textComponent = new JTextArea("", 5, columns);
			addScrollPane = true;
		}
		else if (model.isStyle(TextWidget.PASSWORD_STYLE)) {
			textComponent = new JPasswordField("", columns);
		}
		else {
			textComponent = new JTextField("", columns);
		}
		setToolTip(textComponent);
		getComponent().add(addScrollPane ? new JScrollPane(textComponent) : textComponent);
		limitLength();
		textComponent.getDocument().addDocumentListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isText() &&
			!model.isMultipleChoice() && !model.isMessage();
	}

	// -- Helper methods --

	private void limitLength() {
		// only limit length for single-character inputs
		if (!get().isCharacter()) return;

		// limit text field to a single character
		final int maxChars = 1;
		final Document doc = textComponent.getDocument();
		if (doc instanceof AbstractDocument) {
			final DocumentFilter docFilter = new DocumentSizeFilter(maxChars);
			((AbstractDocument) doc).setDocumentFilter(docFilter);
		}
		else if (log != null) {
			log.warn("Unknown document type: " + doc.getClass().getName());
		}
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (textComponent.getText().equals(text)) return; // no change
		textComponent.setText(text);
	}

}
