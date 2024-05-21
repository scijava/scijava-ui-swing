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
package org.scijava.ui.swing.plot.io;

import org.scijava.plot.Plot;
import org.jfree.chart.JFreeChart;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.scijava.convert.ConvertService;
import org.scijava.io.AbstractIOPlugin;
import org.scijava.io.IOPlugin;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Plugin that can write {@link Plot} as SVG file.
 *
 * @author Matthias Arzt
 */
@Plugin(type = IOPlugin.class)
public class PlotToSvgIOPlugin extends AbstractIOPlugin<Plot> {

	@Parameter
	ConvertService convertService;

	@Override
	public boolean supportsOpen(String source) {
		return false;
	}

	@Override
	public boolean supportsSave(String destination) {
		return destination.endsWith(".svg");
	}

	@Override
	public boolean supportsSave(Object data, String destination) {
		return supportsSave(destination) &&
				data instanceof Plot &&
				convertService.supports(data, JFreeChart.class);
	}

	@Override
	public Plot open(String source) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public void save(Plot data, String destination) throws IOException {
		if(!supportsSave(data, destination))
			throw new IllegalArgumentException();
		JFreeChart chart = convertService.convert(data, JFreeChart.class);
		SVGGraphics2D g = new SVGGraphics2D(data.getPreferredWidth(), data.getPreferredWidth());
		chart.draw(g, new Rectangle(0, 0, g.getWidth(), g.getHeight()));
		SVGUtils.writeToSVG(new File(destination), g.getSVGElement());
	}

	@Override
	public Class<Plot> getDataType() {
		return Plot.class;
	}
}
