/*-
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
package org.scijava.ui.swing.plot;

import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.plotly.Plot;
import tech.tablesaw.plotly.components.*;
import tech.tablesaw.plotly.traces.HistogramTrace;
import tech.tablesaw.plotly.traces.ScatterTrace;

import java.util.Random;

public class TablesawDemo {
	public static void main(String[] args) {
		// Create sample data
		double[] data = new double[1000];
		// Generate mixture of two normal distributions
		Random rand = new Random(42);
		for (int i = 0; i < data.length; i++) {
			if (rand.nextDouble() < 0.7) {
				data[i] = rand.nextGaussian() * 0.5 + 2;  // First peak
			} else {
				data[i] = rand.nextGaussian() * 0.3 + 4;  // Second peak
			}
		}

		// Create a table with the data
		DoubleColumn values = DoubleColumn.create("values", data);
		Table table = Table.create("sample").addColumns(values);

		// Create KDE plot
		Layout layout = Layout.builder()
			.title("Kernel Density Estimate")
			.xAxis(Axis.builder().title("Value").build())
			.yAxis(Axis.builder().title("Density").build())
			.showLegend(true)
			.build();

		// Create histogram for comparison
		HistogramTrace histogram = HistogramTrace.builder(values)
			.name("Histogram")
			.opacity(0.5)
			.nBinsX(30)
			.histNorm(HistogramTrace.HistNorm.PROBABILITY_DENSITY)
			.build();

		// Calculate KDE
		double[] x = new double[200];
		double min = values.min();
		double max = values.max();
		double range = max - min;
		for (int i = 0; i < x.length; i++) {
			x[i] = min + (range * i) / (x.length - 1);
		}

		// Gaussian kernel with Silverman's rule for bandwidth
		double n = data.length;
		double sd = values.standardDeviation();
		double bandwidth = 1.06 * sd * Math.pow(n, -0.2);

		double[] density = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double sum = 0;
			for (double v : data) {
				double z = (x[i] - v) / bandwidth;
				sum += Math.exp(-0.5 * z * z);
			}
			density[i] = sum / (n * bandwidth * Math.sqrt(2 * Math.PI));
		}

		// Create KDE trace
		ScatterTrace kde = ScatterTrace.builder(
				DoubleColumn.create("x", x),
				DoubleColumn.create("density", density))
			.name("KDE")
			.mode(ScatterTrace.Mode.LINE)
			.build();

		// Plot both histogram and KDE
		Plot.show(new Figure(layout, histogram, kde));
	}

	public static void createKDEPlot(DoubleColumn data, String title) {
		// Similar to above, but as a reusable method
		Layout layout = Layout.builder()
			.title(title)
			.xAxis(Axis.builder().title("Value").build())
			.yAxis(Axis.builder().title("Density").build())
			.showLegend(true)
			.build();

		HistogramTrace histogram = HistogramTrace.builder(data)
			.name("Histogram")
			.opacity(0.5)
			.nBinsX(30)
			.histNorm(HistogramTrace.HistNorm.PROBABILITY_DENSITY)
			.build();

		double[] x = new double[200];
		double min = data.min();
		double max = data.max();
		double range = max - min;
		for (int i = 0; i < x.length; i++) {
			x[i] = min + (range * i) / (x.length - 1);
		}

		double n = data.size();
		double sd = data.standardDeviation();
		double bandwidth = 1.06 * sd * Math.pow(n, -0.2);

		double[] density = new double[x.length];
		for (int i = 0; i < x.length; i++) {
			double sum = 0;
			for (double v : data.asDoubleArray()) {
				double z = (x[i] - v) / bandwidth;
				sum += Math.exp(-0.5 * z * z);
			}
			density[i] = sum / (n * bandwidth * Math.sqrt(2 * Math.PI));
		}

		ScatterTrace kde = ScatterTrace.builder(
				DoubleColumn.create("x", x),
				DoubleColumn.create("density", density))
			.name("KDE")
			.mode(ScatterTrace.Mode.LINE)
			.build();

		Plot.show(new Figure(layout, histogram, kde));
	}
}
