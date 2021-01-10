package org.scijava.ui.swing.widget;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComboBox;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.scijava.Context;
import org.scijava.Initializable;
import org.scijava.command.Command;
import org.scijava.command.CommandInfo;
import org.scijava.command.CommandModuleItem;
import org.scijava.command.CommandService;
import org.scijava.command.DynamicCommand;
import org.scijava.command.DynamicCommandInfo;
import org.scijava.log.LogService;
import org.scijava.module.Module;
import org.scijava.module.ModuleService;
import org.scijava.module.MutableModule;
import org.scijava.module.MutableModuleItem;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;
import org.scijava.widget.WidgetService;

public class SwingChoiceWidgetTest {

	private Context context;
	private CommandService commandService;
	private ModuleService moduleService;
	private WidgetService widgetService;

	private static String[] INITIAL_THING_CHOICES = { "a", "b", "c" };
	private static String[] ANIMAL_CHOICES = { "Lion", "Tiger", "Bear" };
	private static String[] VEGETABLE_CHOICES = { "Sage", "Rosemary", "Thyme" };
	private static String[] MINERAL_CHOICES = { "Diamond", "Emerald", "Ruby" };
	private static String ANIMAL = "Animal";
	private static String VEGETABLE = "Vegetable";
	private static String MINERAL = "Mineral";

	@Before
	public void setUp() {
		context = new Context();
		commandService = context.service(CommandService.class);
		moduleService = context.service(ModuleService.class);
		widgetService = context.service(WidgetService.class);
	}

	@After
	public void tearDown() {
		context.dispose();
	}

	@Test
	public void testDynamicCallbacks() {
		CommandInfo info = commandService.getCommand(DynamicCallbacks.class);
		DynamicCommandInfo moduleInfo = new DynamicCommandInfo(info, DynamicCallbacks.class);
		MutableModule module = (MutableModule) moduleService.createModule(moduleInfo);
		MutableModuleItem<String> kindOfThingItem = moduleInfo.getMutableInput("kindOfThing", String.class);
		MutableModuleItem<String> thingItem = moduleInfo.getMutableInput("thing", String.class);

		SwingInputPanel panel = new SwingInputPanel();
		WidgetModel thingWidgetModel = widgetService.createModel(panel, module, thingItem, null);
		SwingInputWidget<String> thingWidget = (SwingInputWidget<String>) widgetService.create(thingWidgetModel);
		WidgetModel kindOfThingWidgetModel = widgetService.createModel(panel, module, kindOfThingItem, null);
		SwingInputWidget<String> kindOfThingWidget = (SwingInputWidget<String>) widgetService.create(kindOfThingWidgetModel);
		panel.refresh();

		assertArrayEquals(INITIAL_THING_CHOICES, thingWidgetModel.getChoices());

		//JComboBox<String> comboBox = (JComboBox<String>) kindOfThingWidget.getComponent().getComponents()[0];
		//comboBox.setSelectedIndex(1);
		kindOfThingWidgetModel.setValue(ANIMAL);
		panel.refresh();
		//kindOfThingWidget.updateModel();
		kindOfThingWidgetModel.callback();
		panel.refresh();
		thingWidget.updateModel();
		panel.refresh();
		thingWidget.refreshWidget();
		panel.refresh();
		System.err.println("First choice now: " + thingItem.getChoices().get(0));
		System.err.println("First choice now: " + thingWidget.get().getChoices()[0]);
		System.err.println(kindOfThingWidget.getComponent().getComponents()[0]);
		System.err.println(thingWidget.getComponent().getComponents()[0]);
		//thingWidget.refreshWidget();
		
		assertArrayEquals(ANIMAL_CHOICES, thingWidgetModel.getChoices());
	}

	@Plugin(type = Command.class)
	public static class DynamicCallbacks extends DynamicCommand {

		@Parameter(callback = "kindOfThingChanged", //
				choices = { "Animal", "Vegetable", "Mineral" })
		private String kindOfThing = "Animal";

		@Parameter(choices = { "a", "b", "c" })
		private String thing = "a";

		@SuppressWarnings("unused")
		private void kindOfThingChanged() {
			context().service(LogService.class).error("callback called with value " + kindOfThing);
			final MutableModuleItem<String> thingItem = //
					getInfo().getMutableInput("thing", String.class);
			switch (kindOfThing) {
			case "Animal":
				thingItem.setChoices(Arrays.asList("Lion", "Tiger", "Bear"));
				break;
			case "Vegetable":
				thingItem.setChoices(Arrays.asList("Sage", "Rosemary", "Thyme"));
				break;
			case "Mineral":
				thingItem.setChoices(Arrays.asList("Diamond", "Emerald", "Ruby"));
				break;
			default:
				thingItem.setChoices(Arrays.asList("???", "WAT", "OHNOEZ"));
				break;
			}
		}
	}

	private void dummy() {
		List<Double> aList = new ArrayList<>();
		aList.add(4.5);
		try {
			List nList = aList.getClass().newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
