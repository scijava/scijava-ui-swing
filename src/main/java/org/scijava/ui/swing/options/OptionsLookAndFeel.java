/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2022 SciJava developers.
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

package org.scijava.ui.swing.options;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Window;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatDarculaLaf;

import org.scijava.display.Display;
import org.scijava.display.DisplayService;
import org.scijava.log.LogService;
import org.scijava.menu.MenuConstants;
import org.scijava.module.MutableModuleItem;
import org.scijava.options.OptionsPlugin;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.ui.UserInterface;
import org.scijava.ui.viewer.DisplayViewer;
import org.scijava.widget.UIComponent;

/**
 * Runs the Edit::Options::Look and Feel dialog.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = OptionsPlugin.class, menu = {
	@Menu(label = MenuConstants.EDIT_LABEL, weight = MenuConstants.EDIT_WEIGHT,
		mnemonic = MenuConstants.EDIT_MNEMONIC),
	@Menu(label = "Options", mnemonic = 'o'),
	@Menu(label = "Look and Feel...", weight = 100, mnemonic = 'l') })
public class OptionsLookAndFeel extends OptionsPlugin {

	// -- Constants --

	private static final String LOOK_AND_FEEL = "lookAndFeel";

	// -- Parameters --

	@Parameter
	private LogService log;

	@Parameter(label = "Look & Feel", persist = false,
		initializer = "initLookAndFeel")
	private String lookAndFeel;

	// -- OptionsLookAndFeel methods --

	public String getLookAndFeel() {
		return lookAndFeel;
	}

	public void setLookAndFeel(final String lookAndFeel) {
		this.lookAndFeel = lookAndFeel;
	}

	// -- Runnable methods --

	@Override
	public void run() {
		// set look and feel
		final LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
		for (final LookAndFeelInfo lookAndFeelInfo : lookAndFeels) {
			if (lookAndFeelInfo.getName().equals(lookAndFeel)) {
				try {
					UIManager.setLookAndFeel(lookAndFeelInfo.getClassName());
					EventQueue.invokeLater(new Runnable() {

						@Override
						public void run() {
							refreshSwingComponents();
						}
					});
				}
				catch (final ClassNotFoundException e) {
					log.error(e);
				}
				catch (final InstantiationException e) {
					log.error(e);
				}
				catch (final IllegalAccessException e) {
					log.error(e);
				}
				catch (final UnsupportedLookAndFeelException e) {
					log.error(e);
				}
				break;
			}
		}

		super.run();
	}

	// -- Initializers --

	protected void initLookAndFeel() {
		final String lafClass = UIManager.getLookAndFeel().getClass().getName();
		LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();

		// Make UIManager aware of FlatLaf look and feels, as needed
		if (!isRegistered(lookAndFeels, FlatLightLaf.NAME)) FlatLightLaf.installLafInfo();
		if (!isRegistered(lookAndFeels, FlatDarkLaf.NAME)) FlatDarkLaf.installLafInfo();
		if (!isRegistered(lookAndFeels, FlatDarculaLaf.NAME)) FlatDarculaLaf.installLafInfo();
		if (!isRegistered(lookAndFeels, FlatIntelliJLaf.NAME)) FlatIntelliJLaf.installLafInfo();
		lookAndFeels = UIManager.getInstalledLookAndFeels(); // retrieve updated list

		final ArrayList<String> lookAndFeelChoices = new ArrayList<>();
		for (final LookAndFeelInfo lafInfo : lookAndFeels) {
			final String lafName = lafInfo.getName();
			lookAndFeelChoices.add(lafInfo.getName());
			if (lafClass.equals(lafInfo.getClassName())) {
				lookAndFeel = lafName;
			}
		}

		final MutableModuleItem<String> lookAndFeelItem =
			getInfo().getMutableInput(LOOK_AND_FEEL, String.class);
		lookAndFeelItem.setChoices(lookAndFeelChoices);
	}

	// -- Helper methods --

	/** Assesses whether lookAndFeels contains the laf associated with lafName*/
	private boolean isRegistered(final LookAndFeelInfo[] lookAndFeels, final String lafName) {
		for (final LookAndFeelInfo lafInfo : lookAndFeels) {
			if (lafInfo.getName().equals(lafName))
				return true;
		}
		return false;
	}

	/** Tells all known Swing components to change to the new Look &amp; Feel. */
	private void refreshSwingComponents() {
		// TODO: Change this hacky logic to call a clean UIService API
		// for window retrieval. But does not exist as of this writing.

		final Set<Component> components = new HashSet<>();

		// add Swing UI components from visible UIs
		for (final UserInterface ui : uiService().getVisibleUIs()) {
			findComponents(components, ui.getApplicationFrame());
			findComponents(components, ui.getConsolePane());
		}

		// add Swing UI components from visible displays
		for (final Display<?> d : displayService().getDisplays()) {
			final DisplayViewer<?> viewer = uiService().getDisplayViewer(d);
			if (viewer == null) continue;
			findComponents(components, viewer.getWindow());
		}

		// refresh all discovered components
		for (final Component c : components) {
			SwingUtilities.updateComponentTreeUI(c);
			if (c instanceof Window) ((Window) c).pack();
		}
	}

	/**
	 * Extracts Swing components from the given object, adding them to
	 * the specified set.
	 */
	private void findComponents(final Set<Component> set, final Object o) {
		if (o == null) return;
		if (o instanceof UIComponent) {
			final UIComponent<?> c = (UIComponent<?>) o;
			findComponents(set, c.getComponent());
		}
		if (o instanceof Window) set.add((Window) o);
		else if (o instanceof Component) {
			final Component c = (Component) o;
			final Window w = SwingUtilities.getWindowAncestor(c);
			set.add(w == null ? c : w);
		}
	}

	private UIService uiService() {
		return getContext().service(UIService.class);
	}

	private DisplayService displayService() {
		return getContext().service(DisplayService.class);
	}

	/**
	 * Sets the application look and feel.
	 * <p>
	 * Useful for setting up the look and feel early on in application startup
	 * routine (e.g., through a macro or script)
	 * </p>
	 * 
	 * @param lookAndFeel the look and feel. Supported values include "FlatLaf
	 *                    Light", "FlatLaf Dark", "FlatLaf Darcula", "FlatLaf
	 *                    IntelliJ", and JVM defaults
	 *                    ("javax.swing.plaf.metal.MetalLookAndFeel", etc.)
	 */
	public static void setupLookAndFeel(final String lookAndFeel) {
		switch (lookAndFeel) { // FIXME: should FlatLaf.updateUI() calls be replaced with updateUILater()?
		case FlatLightLaf.NAME:
			if (FlatLightLaf.setup()) FlatLaf.updateUI();
			return;
		case FlatDarkLaf.NAME:
			if (FlatDarkLaf.setup()) FlatLaf.updateUI();
			return;
		case FlatDarculaLaf.NAME:
			if (FlatDarculaLaf.setup()) FlatLaf.updateUI();
			return;
		case FlatIntelliJLaf.NAME:
			if (FlatIntelliJLaf.setup()) FlatLaf.updateUI();
			return;
		default:
			try {
				UIManager.setLookAndFeel(lookAndFeel);
				FlatLaf.updateUI();
			} catch (final Exception ex) {
				ex.printStackTrace();
				FlatLaf.revalidateAndRepaintAllFramesAndDialogs(); // Recover from possible ill-states
			}
		}
	}

	// -- Deprecated methods --

	@Deprecated
	public UserInterface getUI() {
		return uiService().getDefaultUI();
	}

	@Deprecated
	public void setUI(@SuppressWarnings("unused") final UserInterface ui) {
		throw new UnsupportedOperationException();
	}
}
