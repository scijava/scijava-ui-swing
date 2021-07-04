package org.scijava.ui.swing.plot.converter;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.convert.ConvertService;
import org.scijava.plot.PlotService;
import org.scijava.plot.XYPlot;

import java.awt.image.BufferedImage;

import static org.junit.Assert.assertEquals;

public class PlotToBufferedImageConverterTest
{

	@Test
	public void test() {
		// setup
		Context context = new Context( PlotService.class, ConvertService.class );
		PlotService plotService = context.service( PlotService.class );
		ConvertService convertService = context.service( ConvertService.class );
		XYPlot plot = plotService.newXYPlot();
		// process
		BufferedImage image = convertService.convert( plot, BufferedImage.class );
		// test
		assertEquals(plot.getPreferredWidth(), image.getWidth());
		assertEquals(plot.getPreferredHeight(), image.getHeight());
		// dispose
		context.dispose();
	}
}
