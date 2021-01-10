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
