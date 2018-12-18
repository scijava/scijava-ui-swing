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
