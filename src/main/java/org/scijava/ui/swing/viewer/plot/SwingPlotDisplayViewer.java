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

import org.scijava.plot.Plot;
import org.scijava.ui.viewer.plot.AbstractPlotDisplayViewer;
import org.scijava.convert.ConvertService;
import org.scijava.display.Display;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UserInterface;
import org.scijava.ui.swing.SwingUI;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.ui.viewer.DisplayWindow;
import org.scijava.ui.viewer.plot.PlotDisplay;

/**
 * A Swing {@link Plot} display viewer, which displays plots using JFreeChart.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = DisplayViewer.class)
public class SwingPlotDisplayViewer extends AbstractPlotDisplayViewer {

	@Parameter
	ConvertService convertService;

	@Override
	public boolean isCompatible(final UserInterface ui) {
		return ui instanceof SwingUI;
	}

	@Override
	public boolean canView(final Display<?> d) {
		if(! (d instanceof PlotDisplay ))
			return false;
		Plot plot = ((PlotDisplay) d).get(0);
		return SwingPlotDisplayPanel.supports(plot, convertService);
	}

	@Override
	public void view(final DisplayWindow w, final Display<?> d) {
		super.view(w, d);
		setPanel(new SwingPlotDisplayPanel(getDisplay(), w, convertService));
	}

}
