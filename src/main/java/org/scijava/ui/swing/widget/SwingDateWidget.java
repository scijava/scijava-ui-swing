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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.jdatepicker.ComponentColorDefaults;
import org.jdatepicker.ComponentColorDefaults.Key;
import org.jdatepicker.JDatePicker;
import org.jdatepicker.UtilDateModel;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.DateWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of date selector widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingDateWidget extends SwingInputWidget<Date> implements
	DateWidget<JPanel>, ActionListener
{

	@Parameter
	private LogService log;

	private UtilDateModel dateModel;

	// -- InputWidget methods --

	@Override
	public Date getValue() {
		return dateModel == null ? null : dateModel.getValue();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		dateModel = new UtilDateModel();
		applyThemeColors();
		// NB: the picker must be built on our model, or user picks never reach it
		final JDatePicker datePicker = new JDatePicker(dateModel);
		setToolTip(datePicker);
		getComponent().add(datePicker);
		datePicker.addActionListener(this);

		refreshWidget();
	}

	// -- Helper methods --

	/**
	 * Maps the picker colors to the current look and feel. Note that
	 * {@link ComponentColorDefaults} is a global singleton read when a picker
	 * is created, so existing pickers keep their colors on a theme change
	 */
	private static void applyThemeColors() {
		setColor(Key.BG_MONTH_SELECTOR, "TableHeader.background");
		setColor(Key.FG_MONTH_SELECTOR, "TableHeader.foreground");
		setColor(Key.BG_GRID_HEADER, "TableHeader.background");
		setColor(Key.FG_GRID_HEADER, "TableHeader.foreground");
		setColor(Key.BG_GRID, "Table.background");
		setColor(Key.BG_GRID_NOT_SELECTABLE, "Panel.background");
		setColor(Key.FG_GRID_THIS_MONTH, "Table.foreground");
		setColor(Key.FG_GRID_OTHER_MONTH, "Label.disabledForeground");
		setColor(Key.FG_GRID_TODAY, "Component.accentColor");
		setColor(Key.FG_GRID_SELECTED, "Table.selectionForeground");
		setColor(Key.BG_GRID_SELECTED, "Table.selectionBackground");
		setColor(Key.FG_GRID_TODAY_SELECTED, "Table.selectionForeground");
		setColor(Key.BG_GRID_TODAY_SELECTED, "Table.selectionBackground");
		setColor(Key.BG_TODAY_SELECTOR, "Panel.background");
		setColor(Key.FG_TODAY_SELECTOR_ENABLED, "Label.foreground");
		setColor(Key.FG_TODAY_SELECTOR_DISABLED, "Label.disabledForeground");
		setColor(Key.POPUP_BORDER, "Component.borderColor");
	}

	/** Sets a picker color from a UIManager key, if the key is defined */
	private static void setColor(final Key key, final String uiKey) {
		final Color color = UIManager.getColor(uiKey);
		if (color != null) ComponentColorDefaults.getInstance().setColor(key, color);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(Date.class);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateModel();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Date date = (Date) get().getValue();
		dateModel.setValue(date);
	}

}
