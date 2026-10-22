/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2026 SciJava developers.
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

import java.io.File;
import java.util.Date;

import org.scijava.Context;
import org.scijava.ItemVisibility;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.object.ObjectService;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;
import org.scijava.util.ColorRGB;
import org.scijava.widget.Button;
import org.scijava.widget.ChoiceWidget;
import org.scijava.widget.FileListWidget;
import org.scijava.widget.FileWidget;
import org.scijava.widget.NumberWidget;
import org.scijava.widget.TextWidget;

/**
 * Shows one instance of every widget supported by the Swing UI in a single
 * dialog, for visual testing of layout and alignment (e.g., that all widgets
 * share the same right edge).
 */
public class SwingWidgetLayoutDemo implements Command {

	/** Dummy type to populate the object widget. */
	public static class Thing {

		private final String name;

		public Thing(final String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}
	}

	// -- Message --

	@Parameter(visibility = ItemVisibility.MESSAGE)
	private String message = "Every supported widget, for layout testing";

	// -- Text --

	@Parameter(label = "Text (default)", persist = false)
	private String text = "text";

	@Parameter(label = "Text (field)", style = TextWidget.FIELD_STYLE,
		persist = false)
	private String field = "field";

	@Parameter(label = "Text (password)", style = TextWidget.PASSWORD_STYLE,
		persist = false)
	private String password = "secret";

	@Parameter(label = "Text (area)", style = TextWidget.AREA_STYLE,
		persist = false)
	private String area = "area\nmultiple lines";

	@Parameter(label = "Character", persist = false)
	private Character character = 'c';

	// -- Number --

	@Parameter(label = "Number (spinner)", persist = false)
	private Double spinner = 1.5;

	@Parameter(label = "Number (integer)", min = "0", max = "100",
		persist = false)
	private Integer integer = 10;

	@Parameter(label = "Number (slider)", style = NumberWidget.SLIDER_STYLE,
		min = "0", max = "10", stepSize = "0.01", persist = false)
	private Double slider = 1d;

	@Parameter(label = "Number (scroll bar)",
		style = NumberWidget.SCROLL_BAR_STYLE, min = "0", max = "10",
		stepSize = "0.01", persist = false)
	private Double scrollBar = 1d;

	// -- Boolean --

	@Parameter(label = "Toggle", persist = false)
	private Boolean toggle = true;

	// -- Choices --

	@Parameter(label = "Choice (combo box)", choices = { "One", "Two", "Three" },
		persist = false)
	private String choice = "One";

	@Parameter(label = "Choice (radio, horizontal)",
		style = ChoiceWidget.RADIO_BUTTON_HORIZONTAL_STYLE,
		choices = { "One", "Two", "Three" }, persist = false)
	private String radioH = "One";

	@Parameter(label = "Choice (radio, vertical)",
		style = ChoiceWidget.RADIO_BUTTON_VERTICAL_STYLE,
		choices = { "One", "Two", "Three" }, persist = false)
	private String radioV = "One";

	@Parameter(label = "Object", persist = false)
	private Thing thing;

	// -- Files --

	@Parameter(label = "File (open)", style = FileWidget.OPEN_STYLE,
		persist = false)
	private File open;

	@Parameter(label = "File (open, tif/png)",
		style = FileWidget.OPEN_STYLE + ",extensions:tif/png", persist = false)
	private File openFiltered;

	@Parameter(label = "File (save)", style = FileWidget.SAVE_STYLE,
		persist = false)
	private File save;

	@Parameter(label = "Folder", style = FileWidget.DIRECTORY_STYLE,
		persist = false)
	private File directory;

	@Parameter(label = "File list (files)", style = FileListWidget.FILES_ONLY,
		persist = false)
	private File[] files;

	@Parameter(label = "File list (folders)",
		style = FileListWidget.DIRECTORIES_ONLY, persist = false)
	private File[] folders;

	@Parameter(label = "File list (both)",
		style = FileListWidget.FILES_AND_DIRECTORIES, persist = false)
	private File[] both;

	// -- Other --

	@Parameter(label = "Date", persist = false)
	private Date date = new Date();

	@Parameter(label = "Color", persist = false)
	private ColorRGB color = new ColorRGB(255, 128, 0);

	@Parameter(label = "Button", callback = "onButton", persist = false)
	private Button button;

	@Override
	public void run() {
		// Nothing to do
	}

	@SuppressWarnings("unused")
	private void onButton() {
		System.out.println("Button pressed");
	}

	public static void main(final String... args) throws Exception {
		final Context context = new Context();
		final ObjectService objectService = context.service(ObjectService.class);
		objectService.addObject(new Thing("Thing A"));
		objectService.addObject(new Thing("Thing B"));
		context.service(UIService.class).showUI();
		context.service(CommandService.class).run(SwingWidgetLayoutDemo.class,
			true);
	}
}
