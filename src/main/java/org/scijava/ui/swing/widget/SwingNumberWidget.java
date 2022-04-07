/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2022 SciJava developers.
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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;

import javax.swing.JComponent;
import javax.swing.JLabel;
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
import org.scijava.widget.WidgetStyle;

/**
 * Swing implementation of number chooser widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingNumberWidget extends SwingInputWidget<Number> implements
	NumberWidget<JPanel>, AdjustmentListener, ChangeListener, MouseWheelListener
{

	@Parameter
	private ThreadService threadService;

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private LogService log;

	private CalibratedScrollBar scrollBar;
	private CalibratedSlider slider;
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
		String format = WidgetStyle.getStyleModifier(model.getItem().getWidgetStyle(), "format");
		if (format == null) {
			format = suitableFormat(value, stepSize, min, max);
		}
		spinner.setEditor(new JSpinner.NumberEditor(spinner, format));

		Dimension spinnerSize = spinner.getSize();
		spinnerSize.width = 50;
		spinner.setPreferredSize(spinnerSize);
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
		final Number value = scrollBar.getCalibratedValue();
		spinner.setValue(value);
	}

	// -- ChangeListener methods --

	@Override
	public void stateChanged(final ChangeEvent e) {
		final Object source = e.getSource();
		if (source == slider) {
			// sync spinner with slider value
			final Number value = slider.getCalibratedValue();
			spinner.setValue(value);
		}
		else if (source == spinner) {
			// sync slider and/or scroll bar with spinner value
			syncSliders();
		}
		updateModel();
	}
	
	// -- MouseWheelListener methods --
	
	@Override
	public void mouseWheelMoved(final MouseWheelEvent e) {
		Number value = getValue().doubleValue() + e.getWheelRotation() * get().getStepSize().doubleValue();
		value = Math.min(value.doubleValue(), this.get().getMax().doubleValue());
		value = Math.max(value.doubleValue(), this.get().getMin().doubleValue());
		spinner.setValue(value);
		syncSliders();
	}

	// -- Helper methods --

	private void addScrollBar(final Number min, final Number max,
		final Number step)
	{
		if (min == null || max == null || step == null) {
			log.warn("Invalid min/max/step; cannot render scroll bar");
			return;
		}
		
		// TODO Integer cases can possibly be handled in a simpler way
		int sMin = 0;
		int sMax = (int) ((max.doubleValue() - min.doubleValue()) / step.doubleValue());
		long range = sMax - sMin;
		if (range > Integer.MAX_VALUE) {
			log.warn("Scrollbar span too large; max - min < 2^31 required.");
			return;
		}

		scrollBar = new CalibratedScrollBar(min, max, step);
		setToolTip(scrollBar);
		getComponent().add(scrollBar);
		scrollBar.addAdjustmentListener(this);
		scrollBar.addMouseWheelListener(this);
	}

	private void addSlider(final Number min, final Number max,
		final Number step)
	{
		if (min == null || max == null || step == null) {
			log.warn("Invalid min/max/step; cannot render slider");
			return;
		}

		// TODO Integer cases can possibly be handled in a simpler way
		int sMin = 0;
		int sMax = (int) ((max.doubleValue() - min.doubleValue()) / step.doubleValue());
		long range = sMax - sMin;
		if (range > Integer.MAX_VALUE) {
			log.warn("Slider span too large; max - min < 2^31 required.");
			return;
		}

		slider = new CalibratedSlider(min, max, step);

		setToolTip(slider);
		getComponent().add(slider);
		slider.addChangeListener(this);
		slider.addMouseWheelListener(this);
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
			Number value = getValue();
			if (value.doubleValue() < slider.getCalibratedMinimum().doubleValue()) value = slider.getCalibratedMinimum();
			else if (value.doubleValue() > slider.getCalibratedMaximum().doubleValue()) value = slider.getCalibratedMaximum();
			slider.removeChangeListener(this);
			slider.setCalibratedValue(value);
			slider.addChangeListener(this);
		}
		if (scrollBar != null) {
			// clamp value within scroll bar bounds
			Number value = getValue();
			if (value.doubleValue() < scrollBar.getCalibratedMinimum().doubleValue()) value = scrollBar.getCalibratedMinimum();
			else if (value.doubleValue() > scrollBar.getCalibratedMaximum().doubleValue()) value = scrollBar.getCalibratedMaximum();
			scrollBar.removeAdjustmentListener(this);
			scrollBar.setCalibratedValue(value);
			scrollBar.addAdjustmentListener(this);
		}
	}

	/** Generate a suitable format pattern. */
	private String suitableFormat(Number... values) {
		Integer maxScale = Arrays.stream(values)
				.map(n -> new BigDecimal("" + n.doubleValue()).stripTrailingZeros().scale()).max(Integer::compare)
				.get();
		return formatForScale(maxScale);
	}

	/** Generate a format pattern with sufficient number of decimals. */
	private String formatForScale(int scale) {
		if (scale <= 0) return "0";
		return "0." + String.join("", Collections.nCopies(scale, "0"));
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final Object value = get().getValue();
		if (spinner.getValue().equals(value)) return; // no change
		spinner.setValue(value);
	}
	
	private class CalibratedSlider extends JSlider {
		
		private Number min;
		private Number max;
		private Number stepSize;
		
		private CalibratedSlider(final Number min, final Number max, final Number stepSize) {
			super();
			
			this.min = min;
			this.max = max;
			this.stepSize = stepSize;

			int sMin = 0;
			int sMax = (int) ((max.doubleValue() - min.doubleValue()) / stepSize.doubleValue());

			// Adjust max to be an integer multiple of stepSize
			this.max = min.doubleValue() + (sMax-sMin) * stepSize.doubleValue();

			setMinimum(sMin);
			setMaximum(sMax);
			setValue(sMin);

			// Compute label width to determine number of labels
			int scale = Math.max(0, new BigDecimal(stepSize.toString()).stripTrailingZeros().scale());
			JLabel minLabel = makeLabel(min, scale);
			JLabel maxLabel = makeLabel(max, scale);
			final int labelWidth = Math.max(minLabel.getText().length(), maxLabel.getText().length());

			// Add labels
			Hashtable<Integer, JLabel> labelTable = new Hashtable<>(2);
			labelTable.put(sMin, minLabel);
			labelTable.put(sMax, maxLabel);
			if (labelWidth < 5 && sMax % 5 == 0) {
				// Put four intermediate labels
				labelTable.put(1 * sMax / 5,
						makeLabel(toCalibrated(1 * sMax / 5), scale));
				labelTable.put(2 * sMax / 5,
						makeLabel(toCalibrated(2 * sMax / 5), scale));
				labelTable.put(3 * sMax / 5,
						makeLabel(toCalibrated(3 * sMax / 5), scale));
				labelTable.put(4 * sMax / 5,
						makeLabel(toCalibrated(4 * sMax / 5), scale));
			} else if (labelWidth < 6) {
				// Put three intermediate labels
				labelTable.put(1 * sMax / 4,
						makeLabel(toCalibrated(1 * sMax / 4), scale));
				labelTable.put(2 * sMax / 4,
						makeLabel(toCalibrated(2 * sMax / 4), scale));
				labelTable.put(3 * sMax / 4,
						makeLabel(toCalibrated(3 * sMax / 4), scale));
			}
			setLabelTable(labelTable);
			setPaintLabels(true);
			setMinorTickSpacing(1);
			setPaintTicks(sMax < 100);
		}
		
		private void setCalibratedValue(Number value) {
			setValue(fromCalibrated(value));
		}

		private Number getCalibratedValue() {
			return toCalibrated(getValue());
		}

		private Number getCalibratedMinimum() {
			return min;
		}

		private Number getCalibratedMaximum() {
			return max;
		}
		
		private int fromCalibrated(Number n) {
			return (int) Math.round((n.doubleValue() - min.doubleValue()) / stepSize.doubleValue());
		}
		
		private Number toCalibrated(int n) {
			return n * stepSize.doubleValue() + min.doubleValue();
		}
		
		private JLabel makeLabel(Number n, int scale) {
			return new JLabel(String.format("%." + scale + "f", n.doubleValue()));
		}

	}

	private class CalibratedScrollBar extends JScrollBar {
		
		private Number min;
		private Number max;
		private Number stepSize;
		
		private CalibratedScrollBar(final Number min, final Number max, final Number stepSize) {
			// set extent to 1 to make sure the scroll bar is visible
			super(HORIZONTAL, 0, 1, 0, 1);

			this.min = min;
			this.max = max;
			this.stepSize = stepSize;

			int sMin = 0;
			int sMax = (int) ((max.doubleValue() - min.doubleValue()) / stepSize.doubleValue()) + 1;

			// Adjust max to be an integer multiple of stepSize
			this.max = min.doubleValue() + (sMax-sMin) * stepSize.doubleValue();

			setMinimum(sMin);
			setMaximum(sMax);
			setValue(sMin);
		}
		
		private void setCalibratedValue(Number value) {
			setValue(fromCalibrated(value));
		}

		private Number getCalibratedValue() {
			return toCalibrated(getValue());
		}

		private Number getCalibratedMinimum() {
			return min;
		}

		private Number getCalibratedMaximum() {
			return max;
		}
		
		private int fromCalibrated(Number n) {
			return (int) Math.round((n.doubleValue() - min.doubleValue()) / stepSize.doubleValue());
		}
		
		private Number toCalibrated(int n) {
			return n * stepSize.doubleValue() + min.doubleValue();
		}
		
	}
}
