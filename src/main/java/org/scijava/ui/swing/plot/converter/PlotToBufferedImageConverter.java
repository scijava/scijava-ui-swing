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
@Plugin(type = Converter.class, priority = Priority.NORMAL)
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
