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

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.scijava.module.ModuleCanceledException;
import org.scijava.module.ModuleException;
import org.scijava.module.ModuleItem;
import org.scijava.module.Module;
import org.scijava.module.process.PreprocessorPlugin;
import org.scijava.plugin.Plugin;
import org.scijava.ui.AbstractInputHarvesterPlugin;
import org.scijava.ui.swing.SwingDialog;
import org.scijava.ui.swing.SwingUI;
import org.scijava.widget.InputHarvester;
import org.scijava.widget.InputPanel;
import org.scijava.widget.InputWidget;

/**
 * SwingInputHarvester is an {@link InputHarvester} that collects input
 * parameter values from the user using a {@link SwingInputPanel} dialog box.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
@Plugin(type = PreprocessorPlugin.class, priority = InputHarvester.PRIORITY)
public class SwingInputHarvester extends
	AbstractInputHarvesterPlugin<JPanel, JPanel>
{

	// -- InputHarvester methods --

	@Override
	public SwingInputPanel createInputPanel() {
		return new SwingInputPanel();
	}

	@Override
	public void harvest(final Module module) throws ModuleException {
		final InputPanel<JPanel, JPanel> inputPanel = createInputPanel();
		buildPanel(inputPanel, module);
		if (!inputPanel.hasWidgets()) return;

		// Validate all inputs now so the dialog opens with accurate styling and
		// the OK button already disabled for any initially invalid values.
		for (final ModuleItem<?> item : module.getInfo().inputs()) {
			final InputWidget<?, ?> w = inputPanel.getWidget(item.getName());
			if (w != null) w.get().updateValidation();
		}
		inputPanel.refresh();

		while (true) {
			// Show the dialog; bail out immediately if canceled.
			if (!harvestInputs(inputPanel, module)) throw new ModuleCanceledException();

			// Validate all unresolved inputs and collect any error messages.
			final List<String> errors = new ArrayList<>();
			for (final ModuleItem<?> item : module.getInfo().inputs()) {
				if (module.isInputResolved(item.getName())) continue;
				final String message = item.validateMessage(module);
				if (message != null && !message.isEmpty()) {
					// Use the same label logic as DefaultWidgetModel.getWidgetLabel().
					String label = item.getLabel();
					if (label == null || label.isEmpty()) {
						final String name = item.getName();
						label = name.substring(0, 1).toUpperCase() + name.substring(1);
					}
					errors.add(label + ": " + message);
				}
			}

			if (errors.isEmpty()) break; // all inputs valid; proceed

			// Show a modal error dialog, then re-open the harvester dialog.
			JOptionPane.showMessageDialog(
				null,
				String.join("\n", errors),
				"Invalid Input",
				JOptionPane.ERROR_MESSAGE);
		}

		processResults(inputPanel, module);
	}

	@Override
	public boolean harvestInputs(final InputPanel<JPanel, JPanel> inputPanel,
		final Module module)
	{
		final JPanel pane = inputPanel.getComponent();

		// display input panel in a dialog
		final String title = module.getInfo().getTitle();
		final boolean modal = !module.getInfo().isInteractive();
		final boolean allowCancel = module.getInfo().canCancel();
		final int optionType, messageType;
		if (allowCancel) optionType = JOptionPane.OK_CANCEL_OPTION;
		else optionType = JOptionPane.DEFAULT_OPTION;
		if (inputPanel.isMessageOnly()) {
			if (allowCancel) messageType = JOptionPane.QUESTION_MESSAGE;
			else messageType = JOptionPane.INFORMATION_MESSAGE;
		}
		else messageType = JOptionPane.PLAIN_MESSAGE;
		final boolean doScrollBars = messageType == JOptionPane.PLAIN_MESSAGE;
		final SwingDialog dialog =
			new SwingDialog(pane, optionType, messageType, doScrollBars);
		dialog.setTitle(title);
		dialog.setModal(modal);

		// Wire the OK button to the panel so validation can enable/disable it.
		if (inputPanel instanceof SwingInputPanel) {
			final SwingInputPanel swingPanel = (SwingInputPanel) inputPanel;
			swingPanel.setOkButton(dialog.getOkButton());
			swingPanel.updateOkButton();
		}

		final int rval = dialog.show();

		// Detach the OK button once the dialog closes.
		if (inputPanel instanceof SwingInputPanel) {
			((SwingInputPanel) inputPanel).setOkButton(null);
		}

		// verify return value of dialog
		return rval == JOptionPane.OK_OPTION;
	}

	// -- Internal methods --

	@Override
	protected String getUI() {
		return SwingUI.NAME;
	}

}
