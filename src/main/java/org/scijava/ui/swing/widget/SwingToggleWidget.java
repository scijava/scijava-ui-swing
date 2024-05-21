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

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.ToggleWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of boolean toggle widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingToggleWidget extends SwingInputWidget<Boolean> implements
	ChangeListener, ToggleWidget<JPanel>
{

	private JCheckBox checkBox;

	// -- ChangeListener methods --

	@Override
	public void stateChanged(final ChangeEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public Boolean getValue() {
		return checkBox.isSelected();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		checkBox = new JCheckBox("");
		setToolTip(checkBox);
		getComponent().add(checkBox);
		checkBox.addChangeListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isBoolean();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Boolean value = (Boolean) get().getValue();
		if (value != getValue()) checkBox.setSelected(value != null && value);
	}
}
