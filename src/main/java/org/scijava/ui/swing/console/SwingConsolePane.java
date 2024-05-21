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

package org.scijava.ui.swing.console;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;

import net.miginfocom.swing.MigLayout;

import org.scijava.Context;
import org.scijava.console.OutputEvent;
import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.prefs.PrefService;
import org.scijava.thread.ThreadService;
import org.scijava.ui.console.AbstractConsolePane;
import org.scijava.ui.console.ConsolePane;

/**
 * Swing implementation of {@link ConsolePane}.
 * <p>
 * This implementation consists of a <em>console</em> tab and a <em>log</em>
 * tab, provided by a {@link ConsolePanel} and {@link LoggingPanel}
 * respectively.
 * </p>
 *
 * @author Curtis Rueden
 */
public class SwingConsolePane extends AbstractConsolePane<JPanel> {

	/**
	 * Key to use when when persisting {@link LogFormatter} preferences with the
	 * {@link PrefService}.
	 */
	private static final String LOG_FORMATTING_SETTINGS_KEY = "log-formatting";

	@Parameter
	private Context context;

	@Parameter
	private ThreadService threadService;

	@Parameter
	private LogService logService;

	@Parameter
	private PrefService prefService;

	private ConsolePanel consolePanel;

	private LoggingPanel loggingPanel;

	/**
	 * The console pane's containing window; e.g., a {@link javax.swing.JFrame} or
	 * {@link javax.swing.JInternalFrame}.
	 */
	private Component window;

	private JPanel component;

	private boolean initializing;

	public SwingConsolePane(final Context context) {
		super(context);
	}

	// -- SwingConsolePane methods --

	/** Sets the window which should be shown when {@link #show()} is called. */
	public void setWindow(final Component window) {
		this.window = window;
	}

	public void clear() {
		consolePanel().clear();
	}

	// -- ConsolePane methods --

	@Override
	public void append(final OutputEvent event) {
		consolePanel().outputOccurred(event);
	}

	@Override
	public void show() {
		if (window == null || window.isVisible()) return;
		threadService.queue(() -> window.setVisible(true));
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		if (consolePanel == null) initComponents();
		return component;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

	// -- Helper methods - lazy initialization --

	private ConsolePanel consolePanel() {
		if (consolePanel == null) initComponents();
		return consolePanel;
	}

	private synchronized void initComponents() {
		if (consolePanel != null) return;
		if (initializing) {
			// NB: We are in a loop, with `new ConsolePanel` triggering more output.
			//
			// In a nutshell:
			// stderr -> ConsoleService -> new ConsolePanel -> stderr -> ...
			//
			// Here is an example where we have experienced this happening:
			//
			// ...
			// at org.scijava.ui.swing.console.ConsolePanel.<init>(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.initComponents(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.consolePanel(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.append(...)
			// at org.scijava.ui.console.AbstractConsolePane.outputOccurred(...)
			// at org.scijava.console.DefaultConsoleService.notifyListeners(...)
			// at org.scijava.console.DefaultConsoleService$OutputStreamReporter.publish(...)
			// at org.scijava.console.DefaultConsoleService$OutputStreamReporter.write(...)
			// at org.scijava.console.MultiOutputStream.write(...)
			// at java.io.PrintStream.write(...)
			// ...
			// at java.lang.Throwable.printStackTrace(...)
			// at javax.swing.UIDefaults.getUIError(...)
			// at javax.swing.MultiUIDefaults.getUIError(...)
			// at javax.swing.UIDefaults.getUI(...)
			// at javax.swing.UIManager.getUI(...)
			// at javax.swing.text.JTextComponent.updateUI(...)
			// at javax.swing.text.JTextComponent.<init>(...)
			// at javax.swing.JEditorPane.<init>(...)
			// at javax.swing.JTextPane.<init>(...)
			// at org.scijava.ui.swing.console.ConsolePanel.initGui(...)
			// at org.scijava.ui.swing.console.ConsolePanel.<init>(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.initComponents(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.consolePanel(...)
			// at org.scijava.ui.swing.console.SwingConsolePane.append(...)
			// at org.scijava.ui.console.AbstractConsolePane.outputOccurred(...)
			// at org.scijava.console.DefaultConsoleService.notifyListeners(...)
			// at org.scijava.console.DefaultConsoleService$OutputStreamReporter.publish(...)
			// at org.scijava.console.DefaultConsoleService$OutputStreamReporter.write(...)
			// at org.scijava.console.MultiOutputStream.write(...)
			// at java.io.PrintStream.write(...)
			// ...
			//
			throw new RuntimeException(
					"Output loop while initializing the console GUI.");
		}
		initializing = true;
		consolePanel = new ConsolePanel(context);
		loggingPanel = new LoggingPanel(context, LOG_FORMATTING_SETTINGS_KEY);
		logService.addLogListener(loggingPanel);
		component = new JPanel(new MigLayout("insets 0", "[grow]", "[grow]"));
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Console", consolePanel);
		tabs.addTab("Log", loggingPanel);
		component.add(tabs, "grow");
		initializing = false;
	}

	// -- Helper methods - testing --

	JTextPane getTextPane() {
		return consolePanel().getTextPane();
	}
}
