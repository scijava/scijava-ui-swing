/*-
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2022 SciJava developers.
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
package org.scijava.ui.swing.widget;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

public class SwingNumberWidgetDemo implements Command {

	@Parameter(persist = false)
	private Double a = 0.0000123;

	@Parameter(persist = false)
	private Double b = 0.000123;

	@Parameter(persist = false)
	private Double c = 0.00123;

	@Parameter(persist = false)
	private Double d = 0.0123;

	@Parameter(persist = false)
	private Double e = 0.123;

	@Parameter(persist = false)
	private Double f = 1.23;

	@Parameter(persist = false)
	private Double g = 123d;

	@Parameter(min = "0.0", max = "10.0", stepSize = "0.001", persist = false)
	private Double h = 1d;

	@Parameter(style = "format:#.##", persist = false)
	private Double i = 0.0123;

	@Parameter(style = "format:#.00", persist = false)
	private Double j = 0.0123;

	@Parameter(style = "format:#####.#####", persist = false)
	private Double k = 123.45;

	@Parameter(style = "format:00000.00000", persist = false)
	private Double l = 123.45;

	@Parameter(style = "slider", min = "0", max = "10", stepSize = "0.001", persist = false)
	private Double m = 1d;

	@Parameter(style = "slider,format:0.0000", min = "0", max = "10", stepSize = "0.001", persist = false)
	private Double n = 1d;

	@Parameter(style = "scroll bar", min = "0", max = "10", stepSize = "0.001", persist = false)
	private Double o = 1d;

	@Parameter(style = "scroll bar,format:0.0000", min = "0", max = "10", stepSize = "0.001", persist = false)
	private Double p = 1d;

	@Override
	public void run() {
		// Nothing to do.
	}

	public static void main(final String... args) throws Exception {
		Context context = new Context();
		context.service(UIService.class).showUI();
		context.service(CommandService.class).run(SwingNumberWidgetDemo.class, true);
	}
}
