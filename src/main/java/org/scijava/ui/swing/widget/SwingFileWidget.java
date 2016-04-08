/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2016 Board of Regents of the University of
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

package org.scijava.ui.swing.widget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of file selector widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingFileWidget extends SwingInputWidget<File> implements
	FileWidget<JPanel>, ActionListener, DocumentListener
{

	@Parameter
	private UIService uiService;

	private JTextField path;
	private JButton browse;

	// -- InputWidget methods --

	@Override
	public File getValue() {
		final String text = path.getText();
		return text.isEmpty() ? null : new File(text);
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		path = new JTextField(16);
		setToolTip(path);
		getComponent().add(path);
		path.getDocument().addDocumentListener(this);

		getComponent().add(Box.createHorizontalStrut(3));

		browse = new JButton("Browse");
		setToolTip(browse);
		getComponent().add(browse);
		browse.addActionListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(File.class);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetModel model = get();
		final String style;
		if (model.isStyle(FileWidget.DIRECTORY_STYLE)) {
			style = FileWidget.DIRECTORY_STYLE;
		}
		else if (model.isStyle(FileWidget.SAVE_STYLE)) {
			style = FileWidget.SAVE_STYLE;
		}
		else {
			style = FileWidget.OPEN_STYLE;
		}
		file = uiService.chooseFile(file, style);
		if (file == null) return;

		path.setText(file.getAbsolutePath());
	}

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

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}
}
