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

package org.scijava.ui.swing.widget;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

import net.miginfocom.swing.MigLayout;

import org.scijava.ui.AbstractUIInputWidget;
import org.scijava.ui.UserInterface;
import org.scijava.ui.swing.SwingUI;
import org.scijava.widget.WidgetModel;

/**
 * Common superclass for Swing-based input widgets.
 * 
 * @author Curtis Rueden
 */
public abstract class SwingInputWidget<T> extends
	AbstractUIInputWidget<T, JPanel>
{

	private JPanel uiComponent;
	private Set<JComponent> tooltipped = new HashSet<>();

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		uiComponent = new JPanel();
		final MigLayout layout =
			new MigLayout("fillx,ins 3 0 3 0", "[fill,grow|pref]");
		uiComponent.setLayout(layout);
	}

	// -- InputWidget methods --

	@Override
	public void refreshWidget() {
		super.refreshWidget();
		refreshValidation();
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		return uiComponent;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

	// -- AbstractUIInputWidget methods --

	@Override
	protected UserInterface ui() {
		return ui(SwingUI.NAME);
	}

	// -- Helper methods --

	/** Assigns the model's description as the given component's tool tip. */
	protected void setToolTip(final JComponent c) {
		tooltipped.add(c);
	}

	/**
	 * Updates the widget's border and tooltip to reflect the current validation
	 * state. A red border and tooltip showing the error message are applied when
	 * the model's validation message is non-null; both are cleared when valid.
	 * <p>
	 * Must be called on the Swing EDT.
	 * </p>
	 */
	protected void refreshValidation() {
		final String message = get().getValidationMessage();
		final boolean valid = message == null || message.isEmpty();
		final JPanel p = getComponent();
		final Color borderColor = valid ? p.getBackground() : Color.RED;
		final String tip = valid ? get().getItem().getDescription() : message;
		p.setBorder(BorderFactory.createLineBorder(borderColor, 1));
		tooltipped.forEach(c -> c.setToolTipText(tip));
	}

}
