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

package org.scijava.ui.swing.viewer.plot.jfreechart;

import java.awt.Color;
import java.util.Objects;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.Plot;
import org.scijava.plot.NumberAxis;

/**
 * @author Matthias Arzt
 */
class Utils {

	static JFreeChart setupJFreeChart(String title, Plot plot) {
		JFreeChart chart = new JFreeChart(plot);
		chart.setTitle(title);
		chart.setBackgroundPaint(Color.WHITE);
		chart.getLegend().setFrame(BlockBorder.NONE);
		return chart;
	}

	static ValueAxis getJFreeChartAxis(NumberAxis v) {
		return v.isLogarithmic() ? logarithmicAxis(v) : linearAxis(v);
	}

	static ValueAxis logarithmicAxis(NumberAxis v) {
		LogAxis axis = new LogAxis(v.getLabel());
		switch (v.getRangeStrategy()) {
			case MANUAL:
				axis.setRange(v.getMin(), v.getMax());
				break;
			default:
				axis.setAutoRange(true);
		}
		return axis;
	}

	static ValueAxis linearAxis(NumberAxis v) {
		org.jfree.chart.axis.NumberAxis axis = new org.jfree.chart.axis.NumberAxis(v.getLabel());
		switch(v.getRangeStrategy()) {
			case MANUAL:
				axis.setRange(v.getMin(), v.getMax());
				break;
			case AUTO:
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(false);
				break;
			case AUTO_INCLUDE_ZERO:
				axis.setAutoRange(true);
				axis.setAutoRangeIncludesZero(true);
				break;
			default:
				axis.setAutoRange(true);
		}
		return axis;
	}

	static class SortedLabelFactory {
		private int n;
		SortedLabelFactory() { n = 0; }
		SortedLabel newLabel(Object label) { return new SortedLabel(n++, label); }
	}

	static class SortedLabel implements Comparable<SortedLabel> {
		private final Object label;
		private final int id;
		SortedLabel(final int id, final Object label) { this.label = Objects.requireNonNull(label); this.id = id; }
		@Override public String toString() { return label.toString(); }
		@Override public int compareTo(SortedLabel o) { return Integer.compare(id, o.id); }
		public Object getLabel() { return label; }
	}

}
