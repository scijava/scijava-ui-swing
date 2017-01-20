/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2017 Board of Regents of the University of
 * Wisconsin-Madison.
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

package org.scijava.ui.swing.commands;

import javax.swing.JOptionPane;

import org.scijava.app.AppService;
import org.scijava.command.Command;
import org.scijava.command.ContextCommand;
import org.scijava.menu.MenuConstants;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.swing.SwingDialog;

/**
 * A plugin to display the {@link CommandFinderPanel} in a dialog.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
@Plugin(type = Command.class, menu = {
	@Menu(label = MenuConstants.PLUGINS_LABEL,
		weight = MenuConstants.PLUGINS_WEIGHT,
		mnemonic = MenuConstants.PLUGINS_MNEMONIC), @Menu(label = "Utilities"),
	@Menu(label = "Find Commands...", accelerator = "^L") }, attrs = { @Attr(name = "no-legacy") })
public class CommandFinder extends ContextCommand {

	@Parameter
	private ModuleService moduleService;

	@Parameter
	private AppService appService;

	@Override
	public void run() {
		final String baseDir =
			appService.getApp().getBaseDirectory().getAbsolutePath();
		final CommandFinderPanel commandFinderPanel =
			new CommandFinderPanel(moduleService, baseDir);
		final SwingDialog dialog =
			new SwingDialog(commandFinderPanel, JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE, false);
		dialog.setFocus(commandFinderPanel.getSearchField());
		dialog.setTitle("Find Commands");
		final int rval = dialog.show();
		if (rval != JOptionPane.OK_OPTION) return; // dialog canceled

		final ModuleInfo info = commandFinderPanel.getCommand();
		if (info == null) return; // no command selected

		// execute selected command
		moduleService.run(info, true);
	}

}
