/*
 * 
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2020 SciJava developers.
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
import java.lang.reflect.InvocationTargetException;

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
		// HACK: Work around this hang on macOS:
		// "AWT-EventQueue-0" #14 prio=6 os_prio=31 tid=0x00007fd577a1e800 nid=0x12707 runnable [0x00007000108f5000]
		//   java.lang.Thread.State: RUNNABLE
		//   at sun.lwawt.macosx.CWrapper$NSWindow.isKeyWindow(Native Method)
		//   at sun.lwawt.macosx.CPlatformWindow.lambda$setVisible$9(CPlatformWindow.java:584)
		//   at sun.lwawt.macosx.CPlatformWindow$$Lambda$71/1309956586.run(Unknown Source)
		//   at sun.lwawt.macosx.CFRetainedResource.execute(CFRetainedResource.java:134)
		//   at sun.lwawt.macosx.CPlatformWindow.setVisible(CPlatformWindow.java:576)
		//   at sun.lwawt.LWWindowPeer.setVisibleImpl(LWWindowPeer.java:249)
		//   at sun.lwawt.LWComponentPeer.setVisible(LWComponentPeer.java:765)
		//   at java.awt.Component.show(Component.java:1638)
		//   - locked <0x00000006c0a6bbe8> (a java.awt.Component$AWTTreeLock)
		//   at java.awt.Window.show(Window.java:1042)
		//   at java.awt.Component.show(Component.java:1671)
		//   at java.awt.Component.setVisible(Component.java:1623)
		//   at java.awt.Window.setVisible(Window.java:1014)
		//   at org.scijava.ui.swing.console.SwingConsolePane.lambda$show$0(SwingConsolePane.java:HERE)
		final Runnable windowSetVisible = () -> window.setVisible(true);
		if (System.getProperty("os.name").toLowerCase().startsWith("mac")) {
			try {
				threadService.invoke(windowSetVisible);
			}
			catch (final InterruptedException | InvocationTargetException exc) {
				logService.error(exc);
			}
		}
		else threadService.queue(windowSetVisible);
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		if (consolePanel == null) initLoggingPanel();
		return component;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

	// -- Helper methods - lazy initialization --

	private ConsolePanel consolePanel() {
		if (consolePanel == null) initLoggingPanel();
		return consolePanel;
	}

	private synchronized void initLoggingPanel() {
		if (consolePanel != null) return;
		consolePanel = new ConsolePanel(context);
		loggingPanel = new LoggingPanel(context, LOG_FORMATTING_SETTINGS_KEY);
		logService.addLogListener(loggingPanel);
		component = new JPanel(new MigLayout("", "[grow]", "[grow]"));
		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Console", consolePanel);
		tabs.addTab("Log", loggingPanel);
		component.add(tabs, "grow");
	}

	// -- Helper methods - testing --

	JTextPane getTextPane() {
		return consolePanel().getTextPane();
	}
}
