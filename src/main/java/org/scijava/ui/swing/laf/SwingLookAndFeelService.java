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

package org.scijava.ui.swing.laf;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.prefs.PrefService;
import org.scijava.service.AbstractService;
import org.scijava.service.SciJavaService;
import org.scijava.service.Service;
import org.scijava.ui.UserInterface;

/**
 * SciJava service for Swing Look and Feel configuration.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class SwingLookAndFeelService extends AbstractService implements
	SciJavaService
{

	private static final String LAF_PREF_KEY = "lookAndFeel";

	@Parameter(required = false)
	private PrefService prefs;

	/** Mapping from look-and-feel name to an associated factory. */
	private Map<String, Supplier<LookAndFeel>> factories;

	// -- SwingLookAndFeelService methods --

	/**
	 * Initializes the system-wide Swing look and feel. It is the responsibility
	 * of each {@link UserInterface} implementation to call this before creating
	 * any Swing-based UI components.
	 */
	public void initLookAndFeel() {
		// Set the L+F to match the user setting, or "FlatLaf Light" initially.
		final String laf = prefs == null ? null : //
			prefs.get(getClass(), LAF_PREF_KEY);
		setLookAndFeel(laf == null ? FlatLightLaf.NAME : laf);
	}

	/**
	 * Sets the system-wide look and feel to the given one.
	 * 
	 * @param lookAndFeel the look and feel to set, by either name (e.g. "Metal")
	 *          or class name (e.g. "javax.swing.plaf.metal.MetalLookAndFeel").
	 * @throws IllegalArgumentException If the given look and feel is unknown.
	 */
	public void setLookAndFeel(final String lookAndFeel) {
		if (factories == null) initFactories();

		if (factories.containsKey(lookAndFeel)) {
			// This L+F has a dedicated factory.
			final LookAndFeel laf = factories.get(lookAndFeel).get();
			try {
				UIManager.setLookAndFeel(laf);
			}
			catch (final UnsupportedLookAndFeelException exc) {
				attemptToRecover();
				throw new IllegalArgumentException(//
					"Invalid look and feel: " + lookAndFeel, exc);
			}
		}
		else {
			// No dedicated factory; check for a registered L+F with a matching name.
			final LookAndFeelInfo info = getLookAndFeel(lookAndFeel);

			// If a L+F was found, use it; otherwise assume the argument is a class.
			final String className = info == null ? lookAndFeel : info.getClassName();

			try {
				UIManager.setLookAndFeel(className);
			}
			catch (ClassNotFoundException | InstantiationException
					| IllegalAccessException | UnsupportedLookAndFeelException exc)
			{
				attemptToRecover();
				throw new IllegalArgumentException(//
					"Invalid look and feel: " + lookAndFeel, exc);
			}
		}

		// Update all existing Swing windows to the new L+F.
		FlatLaf.updateUI();

		// Persist L+F setting for next time.
		if (prefs != null) prefs.put(getClass(), LAF_PREF_KEY, lookAndFeel);
	}

	/**
	 * Gets the system-wide look and feel with the given name.
	 * 
	 * @param name Name of the desired look and feel.
	 * @return {@link LookAndFeelInfo} object with the given name.
	 */
	public LookAndFeelInfo getLookAndFeel(final String name) {
		return find(getLookAndFeels(), name);
	}

	/**
	 * Gets the available look and feels.
	 * 
	 * @return Array of {@link LookAndFeelInfo} objects.
	 */
	public LookAndFeelInfo[] getLookAndFeels() {
		// Make UIManager aware of FlatLaf look and feels, as needed.
		final LookAndFeelInfo[] infos = UIManager.getInstalledLookAndFeels();
		if (find(infos, FlatLightLaf.NAME) == null) FlatLightLaf.installLafInfo();
		if (find(infos, FlatDarkLaf.NAME) == null) FlatDarkLaf.installLafInfo();
		if (find(infos, FlatDarculaLaf.NAME) == null) FlatDarculaLaf.installLafInfo();
		if (find(infos, FlatIntelliJLaf.NAME) == null) FlatIntelliJLaf.installLafInfo();

		return UIManager.getInstalledLookAndFeels();
	}

	// -- Helper methods --

	/**
	 * Finds the {@link LookAndFeelInfo} object with the given name, or null if
	 * none.
	 * 
	 * @param infos The list of {@link LookAndFeelInfo}s to search.
	 * @param name The name of the desired look and feel.
	 * @return The matching {@link LookAndFeelInfo}, or null if none.
	 */
	private LookAndFeelInfo find(final LookAndFeelInfo[] infos, final String name) {
		return Arrays.stream(infos)//
			.filter(info -> info.getName().equals(name))//
			.findFirst().orElseGet(() -> null);
	}

	/** Attempts to recover from possible ill-states. */
	private void attemptToRecover() {
		FlatLaf.revalidateAndRepaintAllFramesAndDialogs();
	}

	private synchronized void initFactories() {
		if (factories != null) return;
		final Map<String, Supplier<LookAndFeel>> m = new HashMap<>();
		m.put(FlatLightLaf.NAME, FlatLightLaf::new);
		m.put(FlatDarkLaf.NAME, FlatDarkLaf::new);
		m.put(FlatDarculaLaf.NAME, FlatDarculaLaf::new);
		m.put(FlatIntelliJLaf.NAME, FlatIntelliJLaf::new);
		factories = m;
	}
}
