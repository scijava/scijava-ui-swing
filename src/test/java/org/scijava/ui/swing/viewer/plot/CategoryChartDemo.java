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
import org.scijava.plot.CategoryChart;
import org.scijava.plot.LineSeries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Matthias Arzt
 */
class CategoryChartDemo extends ChartDemo{

	public void run() {

		CategoryChart chart = plotService.newCategoryChart();
		chart.categoryAxis().setManualCategories(Arrays.asList("one wheel", "bicycle", "car"));

		Map<String, Double> wheelsData = new HashMap<>();
		wheelsData.put("one wheel", 1.0);
		wheelsData.put("bicycle", 2.0);
		wheelsData.put("car", 4.0);

		LineSeries lineSeries = chart.addLineSeries();
		lineSeries.setLabel("wheels");
		lineSeries.setValues(wheelsData);

		Map<String, Double> speedData = new HashMap<>();
		speedData.put("one wheel", 10.0);
		speedData.put("bicycle", 30.0);
		speedData.put("car", 200.0);

		BarSeries barSeries = chart.addBarSeries();
		barSeries.setLabel("speed");
		barSeries.setValues(speedData);

		ui.show(chart);
	}

	public static void main(final String... args) {
		new CategoryChartDemo().run();
	}
}
