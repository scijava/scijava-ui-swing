/*-
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
package org.scijava.ui.swing.plot.io;

import org.scijava.Context;
import org.scijava.io.IOService;
import org.scijava.plot.Plot;
import org.scijava.plot.PlotService;
import org.scijava.plot.XYPlot;
import org.scijava.plot.XYSeries;
import org.scijava.plugin.Parameter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author
 */
public class PlotToSvgDemo {

	@Parameter
	private PlotService plotService;

	@Parameter
	private IOService ioService;

	public static void main(String... args) throws IOException {
		PlotToSvgDemo demo = new PlotToSvgDemo();
		new Context().inject(demo);
		demo.run();
	}

	private void run() throws IOException {
		Path path = Paths.get(System.getProperty("user.home"), "chart.svg");
		Plot plot = getExamplePlot();
		ioService.save(plot, path.toString());
		System.out.println("Plot saved as " + path.toString());
	}

	private Plot getExamplePlot() {
		XYPlot plot = plotService.newXYPlot();
		plot.setTitle("Hello World!");
		plot.xAxis().setLabel("x");
		plot.yAxis().setLabel("y");
		List<Double> xs = IntStream.rangeClosed(0, 100).mapToObj(x -> (double) x * 2. * Math.PI / 100.).collect(Collectors.toList());
		List<Double> ys = xs.stream().map(Math::sin).collect(Collectors.toList());
		XYSeries series = plot.addXYSeries();
		series.setLabel("y = sin(x)");
		series.setValues( xs, ys );
		return plot;
	}
}
