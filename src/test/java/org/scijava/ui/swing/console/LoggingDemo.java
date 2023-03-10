/*
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

package org.scijava.ui.swing.console;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import net.miginfocom.swing.MigLayout;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.command.CommandService;
import org.scijava.log.DefaultLogger;
import org.scijava.log.LogSource;
import org.scijava.log.Logger;
import org.scijava.plugin.Parameter;
import org.scijava.ui.UIService;

/**
 * {@link LoggingDemo} is an example to demonstrate the capabilitie of
 * {@link SwingConsolePane} and {@link LoggingPanel}. It shows the scijava gui,
 * and starts two example commands. The first command {@link LoggingLoop} print
 * "Hello World" and logs a message every second. The second command
 * {@link PluginThatLogs} opens a window with its own {@link LoggingPanel} and
 * provides some buttons to emit some example log messages.
 *
 * @author Matthias Arzt
 */
public class LoggingDemo {

	public static void main(String... args) {
		Context context = new Context();
		UIService ui = context.service(UIService.class);
		ui.showUI();
		CommandService commandService = context.service(CommandService.class);
		commandService.run(PluginThatLogs.class, true);
		commandService.run(LoggingLoop.class, true);
	}

	public static class LoggingLoop implements Command {

		@Parameter
		private Logger logger;

		@Override
		public void run() {
			while (true) {
				logger.warn("Message Text");
				System.out.println("Hello World");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.warn(e);
				}
			}
		}
	}

	public static class PluginThatLogs implements Command {

		@Parameter
		private Context context;

		@Parameter
		private Logger log;

		private Logger privateLogger = new DefaultLogger(ignore -> {}, LogSource.newRoot(), 100);

		@Override
		public void run() {
			LoggingPanel panel = new LoggingPanel(context);
			Logger subLogger = log.subLogger("");
			subLogger.addLogListener(panel);
			privateLogger.addLogListener(panel);

			JFrame frame = new JFrame("Plugin that logs");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			frame.setLayout(new MigLayout("","[grow]","[][grow]"));
			frame.add(newButton("log to main window", () -> writeToLogger(log)), "split");
			frame.add(newButton("log to both window", () -> writeToLogger(subLogger)));
			frame.add(newButton("log to this window", () -> writeToLogger(privateLogger)), "wrap");
			frame.add(panel, "grow");
			frame.pack();
			frame.setVisible(true);
		}

		private void writeToLogger(Logger log) {
			log.error("Error message test");
			log.warn("Text describing a warning");
			log.info("An Information");
			log.debug("Something help debugging");
			log.trace("Trace everything");
			log.log(42, "Whats the best log level");
		}

		private Component newButton(String title, Runnable action) {
			JButton button = new JButton(title);
			button.addActionListener(a -> action.run());
			return button;
		}
	}
}
