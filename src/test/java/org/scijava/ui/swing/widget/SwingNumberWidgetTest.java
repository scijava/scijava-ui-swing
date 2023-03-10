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
package org.scijava.ui.swing.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.Collections;

import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.script.ScriptInfo;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;
import org.scijava.widget.WidgetService;

public class SwingNumberWidgetTest {

	private Context context;
	private ModuleService moduleService;
	private WidgetService widgetService;

	@Before
	public void setUp() {
		context = new Context();
		moduleService = context.service(ModuleService.class);
		widgetService = context.service(WidgetService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void test() throws NoSuchFieldException, SecurityException,
		IllegalArgumentException, IllegalAccessException
	{
		String[] expecteds = getExpectedValues();
		Field spinnerField = SwingNumberWidget.class.getDeclaredField("spinner");
		spinnerField.setAccessible(true);
		
		String script = createScript();
		ScriptInfo info = new ScriptInfo(context, ".bsizes", new StringReader(script));
		//System.err.println(moduleService);
		Module module = moduleService.createModule(info);
		//Module module = info.createModule();
		Iterable<ModuleItem<?>> inputs = info.inputs();
		SwingInputPanel inputPanel = new SwingInputPanel();
		int i = 0;
		for (ModuleItem<?> item : inputs) {
			WidgetModel model = widgetService.createModel(inputPanel, module, item, Collections.EMPTY_LIST);
			InputWidget<?, ?> inputWidget = widgetService.create(model);
			assertTrue(inputWidget instanceof SwingNumberWidget);

			JSpinner spinner = (JSpinner) spinnerField.get(inputWidget);
			NumberEditor editor = (NumberEditor) spinner.getEditor();
			assertEquals("Format (index " + i + ")", expecteds[i++], editor.getTextField().getText());
		}
	}

	private String[] getExpectedValues() {
		return new String[] {
				"0.0000123",
				"0.000123",
				"0.00123",
				"0.0123",
				"0.123",
				"1.23",
				"123",
				"1.000",
				"0.01",
				".01",
				"123.45",
				"00123.45000",
				"1.000",
				"1.0000",
				"1.000",
				"1.0000"
		};
	}

	private String createScript() {
		final String script = "// Automatically generated format\n" + 
				"#@ Double (value=0.0000123, persist=false) a\n" + 
				"#@ Double (value=0.000123, persist=false) b\n" + 
				"#@ Double (value=0.00123, persist=false) c\n" + 
				"#@ Double (value=0.0123, persist=false) d\n" + 
				"#@ Double (value=0.123, persist=false) e\n" + 
				"#@ Double (value=1.23, persist=false) f\n" + 
				"#@ Double (value=123, persist=false) g\n" + 
				"#@ Double (value=1, min=0.0, max=10.0, stepSize=0.001, persist=false) h\n" + 
				"\n" + 
				"// Specified format\n" + 
				"#@ Double (value=0.0123, persist=false, style=\"format:#.##\") i\n" + 
				"#@ Double (value=0.0123, persist=false, style=\"format:#.00\") j\n" + 
				"#@ Double (value=123.45, persist=false, style=\"format:#####.#####\") k\n" + 
				"#@ Double (value=123.45, persist=false, style=\"format:00000.00000\") l\n" + 
				"\n" + 
				"// Sliders and scroll bars\n" + 
				"#@ Double (value=1, min=0, max=10, stepSize=0.001, persist=false, style=slider) m\n" + 
				"#@ Double (value=1, min=0, max=10, stepSize=0.001, persist=false, style=\"slider,format:0.0000\") n\n" + 
				"#@ Double (value=1, min=0, max=10, stepSize=0.001, persist=false, style=\"scroll bar\") o\n" + 
				"#@ Double (value=1, min=0, max=10, stepSize=0.001, persist=false, style=\"scroll bar,format:0.0000\") p\n" + 
				"";
		return script;
	}

}
