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

package org.scijava.ui.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import org.scijava.app.AppService;
import org.scijava.display.Display;
import org.scijava.event.EventService;
import org.scijava.menu.MenuService;
import org.scijava.menu.ShadowMenu;
import org.scijava.platform.event.AppMenusCreatedEvent;
import org.scijava.plugin.Parameter;
import org.scijava.ui.AbstractUserInterface;
import org.scijava.ui.SystemClipboard;
import org.scijava.ui.UIService;
import org.scijava.ui.awt.AWTClipboard;
import org.scijava.ui.awt.AWTDropTargetEventDispatcher;
import org.scijava.ui.awt.AWTInputEventDispatcher;
import org.scijava.ui.awt.AWTWindowEventDispatcher;
import org.scijava.ui.console.ConsolePane;
import org.scijava.ui.swing.console.SwingConsolePane;
import org.scijava.ui.swing.menu.SwingJMenuBarCreator;
import org.scijava.ui.swing.menu.SwingJPopupMenuCreator;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.widget.FileWidget;

/**
 * Abstract superclass for Swing-based user interfaces.
 * 
 * @author Curtis Rueden
 * @author Lee Kamentsky
 * @author Barry DeZonia
 * @author Grant Harris
 */
public abstract class AbstractSwingUI extends AbstractUserInterface implements
	SwingUI
{

	@Parameter
	private AppService appService;

	@Parameter
	private EventService eventService;

	@Parameter
	private MenuService menuService;

	@Parameter
	private UIService uiService;

	private SwingApplicationFrame appFrame;
	private SwingToolBar toolBar;
	private SwingStatusBar statusBar;
	private SwingConsolePane consolePane;
	private AWTClipboard systemClipboard;

	// -- UserInterface methods --

	@Override
	public SwingApplicationFrame getApplicationFrame() {
		return appFrame;
	}

	@Override
	public SwingToolBar getToolBar() {
		return toolBar;
	}

	@Override
	public SwingStatusBar getStatusBar() {
		return statusBar;
	}

	@Override
	public SwingConsolePane getConsolePane() {
		return consolePane;
	}

	@Override
	public SystemClipboard getSystemClipboard() {
		return systemClipboard;
	}

	@Override
	public File chooseFile(final File file, final String style) {
		final JFileChooser chooser = new JFileChooser(file);
		if (FileWidget.DIRECTORY_STYLE.equals(style)) {
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		}
		final int rval;
		if (FileWidget.SAVE_STYLE.equals(style)) {
			rval = chooser.showSaveDialog(appFrame);
		}
		else { // default behavior
			rval = chooser.showOpenDialog(appFrame);
		}
		if (rval != JFileChooser.APPROVE_OPTION) return null;
		return chooser.getSelectedFile();
	}

	@Override
	public void showContextMenu(final String menuRoot, final Display<?> display,
		final int x, final int y)
	{
		final ShadowMenu shadowMenu = menuService.getMenu(menuRoot);

		final JPopupMenu popupMenu = new JPopupMenu();
		new SwingJPopupMenuCreator().createMenus(shadowMenu, popupMenu);

		final DisplayViewer<?> displayViewer = uiService.getDisplayViewer(display);
		if (displayViewer != null) {
			final Component invoker = (Component) displayViewer.getPanel();
			popupMenu.show(invoker, x, y);
		}
	}

	@Override()
	public boolean requiresEDT() {
		return true;
	}

	// -- Disposable methods --

	@Override
	public void dispose() {
		if (appFrame != null) appFrame.dispose();
	}

	// -- Internal methods --

	@Override
	protected void createUI() {
		final JMenuBar menuBar = createMenus();

		appFrame = new SwingApplicationFrame(appService.getApp().getTitle());
		if (menuBar != null) appFrame.setJMenuBar(menuBar);

		toolBar = new SwingToolBar(getContext());
		statusBar = new SwingStatusBar(getContext());

		if (!Boolean.getBoolean(ConsolePane.NO_CONSOLE_PROPERTY)) {
			consolePane = new SwingConsolePane(getContext());
		}

		systemClipboard = new AWTClipboard();

		setupAppFrame();

		super.createUI();

		// NB: The following setup happens for both SDI and MDI frames.

		appFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		appFrame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(final WindowEvent evt) {
				appService.getApp().quit();
			}

		});

		appFrame.getContentPane().add(toolBar, BorderLayout.NORTH);
		appFrame.getContentPane().add(statusBar, BorderLayout.SOUTH);

		// listen for input events on all components of the app frame
		final AWTInputEventDispatcher inputDispatcher =
			new AWTInputEventDispatcher(null, eventService);
		appFrame.addEventDispatcher(inputDispatcher);
		statusBar.addEventDispatcher(inputDispatcher);

		// listen for window events on the app frame
		final AWTWindowEventDispatcher windowDispatcher =
			new AWTWindowEventDispatcher(eventService);
		windowDispatcher.register(appFrame);

		// listen for drag and drop events
		final AWTDropTargetEventDispatcher dropTargetDispatcher =
			new AWTDropTargetEventDispatcher(null, eventService);
		dropTargetDispatcher.register(toolBar);
		dropTargetDispatcher.register(statusBar);
		dropTargetDispatcher.register(appFrame);

		setupConsole();

		appFrame.pack();
		appFrame.setVisible(true);
	}

	/**
	 * Creates a {@link JMenuBar} from the master {@link ShadowMenu} structure.
	 */
	protected JMenuBar createMenus() {
		final JMenuBar menuBar =
			menuService.createMenus(new SwingJMenuBarCreator(), new JMenuBar());
		final AppMenusCreatedEvent appMenusCreatedEvent =
			new AppMenusCreatedEvent(menuBar);
		eventService.publish(appMenusCreatedEvent);
		if (appMenusCreatedEvent.isConsumed()) {
			// something else (e.g., MacOSXPlatform) handled the menus
			return null;
		}
		appMenusCreatedEvent.consume();
		return menuBar;
	}

	protected JMenuBar createConsoleMenu() {
		final JMenuBar menuBar = new JMenuBar();
		final JMenu edit = new JMenu("Edit");
		menuBar.add(edit);
		final JMenuItem editClear = new JMenuItem("Clear");
		editClear.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getConsolePane().clear();
			}
		});
		edit.add(editClear);
		return menuBar;
	}

	/**
	 * Configures the application frame for subclass-specific settings (e.g., SDI
	 * or MDI).
	 */
	protected abstract void setupAppFrame();

	/**
	 * Configures the console for subclass-specific settings (e.g., SDI or MDI).
	 */
	protected abstract void setupConsole();

}
