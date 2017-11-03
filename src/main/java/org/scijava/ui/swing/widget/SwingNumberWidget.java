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

import java.awt.Adjustable;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.scijava.log.LogService;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.thread.ThreadService;
import org.scijava.widget.InputWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingNumberWidget extends SwingInputWidget<Number> implements
	NumberWidget<JPanel>, AdjustmentListener, ChangeListener
{

	@Parameter
	private ThreadService threadService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private LogService log;

	private JScrollBar scrollBar;
	private JSlider slider;
	private JSpinner spinner;

	// -- InputWidget methods --

	@Override
	public Number getValue() {
		return (Number) spinner.getValue();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final Number min = model.getMin();
		final Number max = model.getMax();
		final Number softMin = model.getSoftMin();
		final Number softMax = model.getSoftMax();
		final Number stepSize = model.getStepSize();

		// add optional widgets, if specified
		if (model.isStyle(NumberWidget.SCROLL_BAR_STYLE)) {
			addScrollBar(softMin, softMax, stepSize);
		}
		else if (model.isStyle(NumberWidget.SLIDER_STYLE)) {
			addSlider(softMin, softMax, stepSize);
		}

		// add spinner widget
		final Class<?> type = model.getItem().getType();
		final Number value = (Number) model.getValue();
		final SpinnerNumberModel spinnerModel =
			new SpinnerNumberModelFactory().createModel(value, min, max, stepSize);
		spinner = new JSpinner(spinnerModel);
		fixSpinner(type);
		setToolTip(spinner);
		getComponent().add(spinner);
		limitWidth(200);
		spinner.addChangeListener(this);

		refreshWidget();
		syncSliders();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isNumber();
	}

	// -- AdjustmentListener methods --

	@Override
	public void adjustmentValueChanged(final AdjustmentEvent e) {
		// sync spinner with scroll bar value
		final int value = scrollBar.getValue();
		spinner.setValue(value);
	}

	// -- ChangeListener methods --

	@Override
	public void stateChanged(final ChangeEvent e) {
		final Object source = e.getSource();
		if (source == slider) {
			// sync spinner with slider value
			final int value = slider.getValue();
			spinner.setValue(value);
		}
		else if (source == spinner) {
			// sync slider and/or scroll bar with spinner value
			syncSliders();
		}
		updateModel();
	}

	// -- Helper methods --

	private void addScrollBar(final Number min, final Number max,
		final Number step)
	{
		if (min == null || max == null || step == null) {
			log.warn("Invalid min/max/step; cannot render scroll bar");
			return;
		}
		int mn = min.intValue();
		if (mn == Integer.MIN_VALUE) mn = Integer.MIN_VALUE + 1;
		int mx = max.intValue();
		if (mx < Integer.MAX_VALUE) mx++;
		final int st = step.intValue();

		scrollBar = new JScrollBar(Adjustable.HORIZONTAL, mn, 1, mn, mx);
		scrollBar.setUnitIncrement(st);
		setToolTip(scrollBar);
		getComponent().add(scrollBar);
		scrollBar.addAdjustmentListener(this);
	}

	private void addSlider(final Number min, final Number max,
		final Number step)
	{
		if (min == null || max == null || step == null) {
			log.warn("Invalid min/max/step; cannot render slider");
			return;
		}
		final int mn = min.intValue();
		final int mx = max.intValue();
		final int st = step.intValue();
		if ((long) mx - mn > Integer.MAX_VALUE) {
			log.warn("Slider span too large; max - min < 2^31 required.");
			return;
		}
		final int span = mx - mn;

		slider = new JSlider(mn, mx, mn);

		// Compute optimal major ticks and labels.
		final int labelWidth = Math.max(("" + mn).length(), ("" + mx).length());
		slider.setMajorTickSpacing(labelWidth < 5 ? span / 4 : span);
		slider.setPaintLabels(labelWidth < 10);

		// Compute optimal minor ticks.
		final int stepCount = span / st + 1;
		slider.setMinorTickSpacing(st);
		slider.setPaintTicks(stepCount < 100);

		setToolTip(slider);
		getComponent().add(slider);
		slider.addChangeListener(this);
	}

	/**
	 * Limit component width to a certain maximum. This is a HACK to work around
	 * an issue with Double-based spinners that attempt to size themselves very
	 * large (presumably to match Double.MAX_VALUE).
	 */
	private void limitWidth(final int maxWidth) {
		final Dimension minSize = spinner.getMinimumSize();
		if (minSize.width > maxWidth) {
			minSize.width = maxWidth;
			spinner.setMinimumSize(minSize);
		}
		final Dimension prefSize = spinner.getPreferredSize();
		if (prefSize.width > maxWidth) {
			prefSize.width = maxWidth;
			spinner.setPreferredSize(prefSize);
		}
	}

	/** Improves behavior of the {@link JSpinner} widget. */
	private void fixSpinner(final Class<?> type) {
		fixSpinnerType(type);
		fixSpinnerFocus();
	}

	/**
	 * Fixes spinners that display {@link BigDecimal} or {@link BigInteger}
	 * values. This is a HACK to work around the fact that
	 * {@link DecimalFormat#parse(String, ParsePosition)} uses {@link Double}
	 * and/or {@link Long} by default, hence losing precision.
	 */
	private void fixSpinnerType(final Class<?> type) {
		if (!BigDecimal.class.isAssignableFrom(type) &&
			!BigInteger.class.isAssignableFrom(type))
		{
			return;
		}
		final JComponent editor = spinner.getEditor();
		final JSpinner.NumberEditor numberEditor = (JSpinner.NumberEditor) editor;
		final DecimalFormat decimalFormat = numberEditor.getFormat();
		decimalFormat.setParseBigDecimal(true);
	}

	/**
	 * Tries to ensure that the text of a {@link JSpinner} becomes selected when
	 * it first receives the focus.
	 * <p>
	 * Adapted from <a href="http://stackoverflow.com/q/20971050">this SO
	 * post</a>.
	 */
	private void fixSpinnerFocus() {
		for (final Component c : spinner.getEditor().getComponents()) {
			if (!(c instanceof JTextField)) continue;
			final JTextField textField = (JTextField) c;

			textField.addFocusListener(new FocusListener() {

				@Override
				public void focusGained(final FocusEvent e) {
					queueSelection();
				}

				@Override
				public void focusLost(final FocusEvent e) {
					queueSelection();
				}

				private void queueSelection() {
					threadService.queue(new Runnable() {

						@Override
						public void run() {
							textField.selectAll();
						}
					});
				}

			});
		}
	}

	/** Sets slider values to match the spinner. */
	private void syncSliders() {
		if (slider != null) {
			// clamp value within slider bounds
			int value = getValue().intValue();
			if (value < slider.getMinimum()) value = slider.getMinimum();
			else if (value > slider.getMaximum()) value = slider.getMaximum();
			slider.removeChangeListener(this);
			slider.setValue(value);
			slider.addChangeListener(this);
		}
		if (scrollBar != null) {
			// clamp value within scroll bar bounds
			int value = getValue().intValue();
			if (value < scrollBar.getMinimum()) value = scrollBar.getMinimum();
			else if (value > scrollBar.getMaximum()) value = scrollBar.getMaximum();
			scrollBar.removeAdjustmentListener(this);
			scrollBar.setValue(getValue().intValue());
			scrollBar.addAdjustmentListener(this);
		}
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Object value = get().getValue();
		if (spinner.getValue().equals(value)) return; // no change
		spinner.setValue(value);
	}
}
