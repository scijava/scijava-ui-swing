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

package org.scijava.ui.swing.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.scijava.input.Accelerator;
import org.scijava.input.KeyCode;
import org.scijava.menu.AbstractMenuCreator;
import org.scijava.menu.ShadowMenu;
import org.scijava.module.ModuleInfo;

/**
 * Populates a Swing menu structure with menu items from a {@link ShadowMenu}.
 * <p>
 * This class is called {@code AbstractSwingMenuCreator} rather than simply
 * {@code AbstractMenuCreator} to avoid having multiple classes with the same
 * name in different packages (e.g., {@code imagej.menu.AbstractMenuCreator} and
 * {@code imagej.ui.swing.menu.AbstractMenuCreator}).
 * </p>
 * 
 * @author Curtis Rueden
 */
public abstract class AbstractSwingMenuCreator<T> extends
	AbstractMenuCreator<T, JMenu>
{

	/** Table of button groups for radio button menu items. */
	private HashMap<String, ButtonGroup> buttonGroups = new HashMap<>();

	// -- MenuCreator methods --

	@Override
	public void createMenus(final ShadowMenu root, final T target) {
		buttonGroups = new HashMap<>();
		super.createMenus(root, target);
	}

	// -- Internal methods --

	@Override
	protected void addLeafToMenu(final ShadowMenu shadow, final JMenu target) {
		final JMenuItem menuItem = createLeaf(shadow);
		target.add(menuItem);
	}

	@Override
	protected JMenu addNonLeafToMenu(final ShadowMenu shadow, final JMenu target)
	{
		final JMenu menu = createNonLeaf(shadow);
		target.add(menu);
		return menu;
	}

	@Override
	protected void addSeparatorToMenu(final JMenu target) {
		target.addSeparator();
	}

	protected JMenuItem createLeaf(final ShadowMenu shadow) {
		final String name = shadow.getMenuEntry().getName();
		final JMenuItem menuItem;
		// CTR TEMP - disable checkbox menu items for beta1 release
		//if (shadow.isCheckBox()) {
		//	menuItem = new JCheckBoxMenuItem(name, isSelected(shadow));
		//}
		//else if (shadow.isRadioButton()) {
		//	menuItem = new JRadioButtonMenuItem(name, isSelected(shadow));
		//	getButtonGroup(shadow).add(menuItem);
		//}
		//else
		menuItem = new JMenuItem(name);
		assignProperties(menuItem, shadow);
		linkAction(shadow, menuItem);
		return menuItem;
	}

	protected JMenu createNonLeaf(final ShadowMenu shadow) {
		final JMenu menu = new JMenu(shadow.getMenuEntry().getName());
		assignProperties(menu, shadow);
		return menu;
	}

	// -- Helper methods --

	private boolean isSelected(final ShadowMenu shadow) {
		return shadow.getModuleInfo().isSelected();
	}

	private ButtonGroup getButtonGroup(final ShadowMenu shadow) {
		final String selectionGroup = shadow.getModuleInfo().getSelectionGroup();
		ButtonGroup buttonGroup = buttonGroups.get(selectionGroup);
		if (buttonGroup == null) {
			buttonGroup = new ButtonGroup();
			buttonGroups.put(selectionGroup, buttonGroup);
		}
		return buttonGroup;
	}

	private KeyStroke getKeyStroke(final ShadowMenu shadow) {
		final Accelerator accelerator = shadow.getMenuEntry().getAccelerator();
		if (accelerator == null || accelerator.getKeyCode() == KeyCode.UNDEFINED) return null;
		return KeyStroke.getKeyStroke(accelerator.toString());
	}

	private Icon loadIcon(final ShadowMenu shadow) {
		final URL iconURL = shadow.getIconURL();
		return iconURL == null ? null : new ImageIcon(iconURL);
	}

	private void assignProperties(final JMenuItem menuItem,
		final ShadowMenu shadow)
	{
		final char mnemonic = shadow.getMenuEntry().getMnemonic();
		if (mnemonic != '\0') menuItem.setMnemonic(mnemonic);

		final KeyStroke keyStroke = getKeyStroke(shadow);
		if (keyStroke != null) menuItem.setAccelerator(keyStroke);

		final Icon icon = loadIcon(shadow);
		if (icon != null) menuItem.setIcon(icon);

		final ModuleInfo info = shadow.getModuleInfo();
		if (info != null) menuItem.setEnabled(info.isEnabled());
	}

	private void linkAction(final ShadowMenu shadow, final JMenuItem menuItem) {
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				shadow.run();
			}
		});
	}

}
