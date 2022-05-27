package org.scijava.ui.swing.widget;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.command.CommandInfo;
import org.scijava.command.ContextCommand;
import org.scijava.module.Module;
import org.scijava.module.ModuleItem;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Parameter;
import org.scijava.widget.InputPanel;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;
import org.scijava.widget.WidgetService;

public class SwingObjectWidgetTest {

	private Context context;
	private ModuleService moduleService;
	private WidgetService widgetService;

	@Before
	public void setUp() {
		context = new Context();
		moduleService = context.getService(ModuleService.class);
		widgetService = context.getService(WidgetService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testObjects() {
		Thing a = new Thing();
		Thing b = new Thing();

		CommandInfo commandInfo = new CommandInfo(MyCommand.class);
		Module module = moduleService.createModule(commandInfo);
		InputPanel<?,?> panel = new SwingInputPanel();

		ModuleItem<Thing> thingInput = moduleService.getSingleInput(module, Thing.class);
		ModuleItem<Nothing> nothingInput = moduleService.getSingleInput(module, Nothing.class);
		ModuleItem<Choices> choicesInput = moduleService.getSingleInput(module, Choices.class);

		WidgetModel thingModel = widgetService.createModel(panel, module, thingInput, Arrays.asList(a, b));
		WidgetModel nothingModel = widgetService.createModel(panel, module, nothingInput, null);
		WidgetModel choicesModel = widgetService.createModel(panel, module, choicesInput, null);

		InputWidget<?, ?> thingWidget = widgetService.create(thingModel);
		assertTrue(thingWidget instanceof SwingObjectWidget);

		InputWidget<?, ?> nothingWidget = widgetService.create(nothingModel);
		assertFalse(nothingWidget instanceof SwingObjectWidget);

		InputWidget<?, ?> choicesWidget = widgetService.create(choicesModel);
		assertTrue(choicesWidget instanceof SwingObjectWidget);
	}

	private class Thing {
		// dummy class
	}

	private class Nothing {
		// dummy class
	}

	private enum Choices {
		FIRST, SECOND, THIRD
	};

	public static class MyCommand extends ContextCommand {
		@Parameter
		private Thing thing;

		@Parameter
		private Nothing nothing;

		@Parameter
		private Choices choices;

		@Override
		public void run() {
			// nothing to do
		}

	}
}
