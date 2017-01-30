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

package org.scijava.ui.swing.widget;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.scijava.plugin.Plugin;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of multiple choice selector widget using
 * {@link JRadioButton}s.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class, priority = SwingChoiceWidget.PRIORITY + 1)
public class SwingChoiceRadioWidget extends SwingInputWidget<Object> implements
	ActionListener, ChoiceWidget<JPanel>
{

	private List<DataJRadioButton> radioButtons;

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public Object getValue() {
		final DataJRadioButton selectedButton = getSelectedButton();
		return selectedButton == null ? null : selectedButton.getData();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final List<?> items = model.getItem().getChoices();

		final ButtonGroup buttonGroup = new ButtonGroup();
		final JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, getBoxAxis(model)));
		radioButtons = new ArrayList<>(items.size());

		for (final Object item : items) {
			final DataJRadioButton radioButton = new DataJRadioButton(item);
			setToolTip(radioButton);
			radioButton.addActionListener(this);

			buttonGroup.add(radioButton);
			buttonPanel.add(radioButton);
			radioButtons.add(radioButton);
		}
		getComponent().add(buttonPanel);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isMultipleChoice() &&
			isRadioButtonStyle(model);
	}

	// -- Helper methods --

	private boolean isRadioButtonStyle(final WidgetModel model) {
		return model.isStyle(ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE) ||
			model.isStyle(ChoiceWidget.RADIO_BUTTON_VERTICAL_STYLE);
	}

	private int getBoxAxis(final WidgetModel model) {
		if (model.isStyle(ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE)) {
			return BoxLayout.X_AXIS;
		}
		if (model.isStyle(ChoiceWidget.RADIO_BUTTON_VERTICAL_STYLE)) {
			return BoxLayout.Y_AXIS;
		}
		throw new IllegalStateException("Invalid widget style: " +
			model.getItem().getWidgetStyle());
	}

	private DataJRadioButton getSelectedButton() {
		for (final DataJRadioButton radioButton : radioButtons) {
			if (radioButton.isSelected()) return radioButton;
		}
		return null;
	}

	private DataJRadioButton getButton(final Object value) {
		for (final DataJRadioButton radioButton : radioButtons) {
			if (radioButton.getData() == value) return radioButton;
		}
		return null;
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Object value = get().getValue();
		final DataJRadioButton radioButton = getButton(value);
		if (radioButton.isSelected()) return; // no change
		radioButton.setSelected(true);
	}
	
	// -- Helper classes --

	/** A {@link JRadioButton} with a backing data object. */
	private class DataJRadioButton extends JRadioButton {

		private Object data;

		private DataJRadioButton(final Object data) {
			this(data, data.toString());
		}

		private DataJRadioButton(final Object data, final String text) {
			super(text);
			this.data = data;
		}

		public Object getData() {
			return data;
		}
	}
}
