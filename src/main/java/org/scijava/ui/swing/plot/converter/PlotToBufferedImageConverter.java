package org.scijava.ui.swing.plot.converter;

import org.jfree.chart.JFreeChart;
import org.scijava.Priority;
import org.scijava.convert.AbstractConverter;
import org.scijava.convert.ConversionRequest;
import org.scijava.convert.ConvertService;
import org.scijava.convert.Converter;
import org.scijava.plot.Plot;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * Converter plugin, that converts an {@link Plot} to {@link BufferedImage}.
 *
 * @author Matthias Arzt
 * @see ConvertService
 */
@Plugin(type = Converter.class, priority = Priority.NORMAL_PRIORITY)
public class PlotToBufferedImageConverter extends AbstractConverter<Plot, BufferedImage>
{

	@Parameter
	ConvertService convertService;

	@Override
	public boolean canConvert(ConversionRequest request) {
		return request.destClass().isAssignableFrom( BufferedImage.class ) &&
				Plot.class.isAssignableFrom( request.sourceClass() ) &&
				convertService.supports(new ConversionRequest(
						request.sourceObject(), request.sourceType(), JFreeChart.class));
	}

	@Override
	public <T> T convert(Object o, Class<T> aClass) {
		if(o instanceof Plot && BufferedImage.class.equals(aClass)) {
			@SuppressWarnings("unchecked")
			T t = (T) toBufferedImage((Plot) o);
			return t;
		}
		return null;
	}

	private BufferedImage toBufferedImage(Plot plot) {
		BufferedImage image = new BufferedImage( plot.getPreferredWidth(), plot.getPreferredHeight(), BufferedImage.TYPE_INT_ARGB );
		JFreeChart chart = convertService.convert(plot, JFreeChart.class);
		chart.draw(image.createGraphics(), new Rectangle2D.Float(0, 0, image.getWidth(), image.getHeight()));
		return image;
	}

	@Override
	public Class<BufferedImage> getOutputType() {
		return BufferedImage.class;
	}

	@Override
	public Class<Plot> getInputType() {
		return Plot.class;
	}
}
