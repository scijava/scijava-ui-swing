/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2023 SciJava developers.
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

package org.scijava.ui.swing.viewer.plot;

import org.scijava.plot.BarSeries;
import org.scijava.plot.CategoryAxis;
import org.scijava.plot.CategoryChart;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Matthias Arzt
 */

class SortingCategoriesDemo extends ChartDemo {

	public void run() {
		showSortedCategoryChart( axis -> {
			axis.setManualCategories(Arrays.asList("a","c","b"));
			axis.setLabel("acb");
		} );
		showSortedCategoryChart( axis -> {
			axis.setManualCategories(Arrays.asList("a","g","c","b"));
			axis.setLabel("agcb");
		} );
		showSortedCategoryChart( axis -> {
			axis.setManualCategories(Arrays.asList("d","c","a","b"));
			axis.setOrder( String::compareTo );
			axis.setLabel("abcd");
		} );
		showSortedCategoryChart( axis -> {
			axis.setManualCategories(Collections.emptyList());
			axis.setOrder( String::compareTo );
			axis.setLabel("empty");
		} );
	}

	private interface AxisManipulator {
		void manipulate( CategoryAxis axis );
	}

	private void showSortedCategoryChart(AxisManipulator categoryAxisManipulator) {
		CategoryChart chart = plotService.newCategoryChart();
		categoryAxisManipulator.manipulate(chart.categoryAxis());

		Map<String, Double> data = new TreeMap<>();
		data.put("a", 1.0);
		data.put("b", 2.0);
		data.put("c", 3.0);
		data.put("d", 4.0);

		BarSeries bars = chart.addBarSeries();
		bars.setValues(data);

		ui.show(chart);
	}

	public static void main(final String... args) {
		new SortingCategoriesDemo().run();
	}

}
