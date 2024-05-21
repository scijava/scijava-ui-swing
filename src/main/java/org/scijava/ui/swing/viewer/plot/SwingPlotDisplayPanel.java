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

package org.scijava.ui.swing.viewer.plot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.scijava.convert.ConvertService;
import org.scijava.plot.Plot;
import org.scijava.ui.viewer.DisplayWindow;
import org.scijava.ui.viewer.plot.PlotDisplay;
import org.scijava.ui.viewer.plot.PlotDisplayPanel;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * A JFreeChart-driven display panel for {@link Plot}s.
 * 
 * @author Curtis Rueden
 */
public class SwingPlotDisplayPanel extends JPanel implements PlotDisplayPanel
{

	// -- instance variables --

	private final DisplayWindow window;
	private final PlotDisplay display;
	private final ConvertService convertService;
	private Dimension prefferedSize;

	// -- constructor --

	public SwingPlotDisplayPanel(final PlotDisplay display,
								 final DisplayWindow window, final ConvertService convertService)
	{
		this.display = display;
		this.window = window;
		this.convertService = convertService;
		setLayout(new BorderLayout());
		initPreferredSize();
		setupChart();
		window.setContent(this);
	}

	private void initPreferredSize() {
		Plot plot = display.get(0);
		prefferedSize = new Dimension(plot.getPreferredWidth(), plot.getPreferredHeight());
	}

	private void setupChart() {
		final JFreeChart chart = convertToJFreeChart(display.get(0));
		add(new ChartPanel(chart));
	}

	private JFreeChart convertToJFreeChart(Plot plot) {
		return Objects.requireNonNull(convertService.convert(plot, JFreeChart.class));
	}

	public static boolean supports(Plot abstractPlot, ConvertService convertService) {
		return convertService.supports(abstractPlot, JFreeChart.class);
	}

	// -- PlotDisplayPanel methods --

	@Override
	public PlotDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() { }

	@Override
	public void setLabel(final String s) { }

	@Override
	public void redraw() { }

	@Override
	public Dimension getPreferredSize() {
		return prefferedSize;
	}

}
