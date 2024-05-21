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

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.scijava.menu.ShadowMenu;

/**
 * Populates a {@link JMenu} with menu items from a {@link ShadowMenu}.
 * <p>
 * Unfortunately, the {@link SwingJMenuBarCreator}, {@link SwingJMenuCreator}
 * and {@link SwingJPopupMenuCreator} classes must all exist and replicate some
 * code, because {@link JMenuBar}, {@link JMenuItem} and {@link JPopupMenu} do
 * not share a common interface for operations such as {@link JMenu#add}.
 * </p>
 * <p>
 * This class is called {@code SwingJMenuCreator} rather than simply
 * {@code JMenuCreator} for consistency with other UI implementations such as
 * {@code imagej.ui.awt.menu.AWTMenuCreator}.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class SwingJMenuCreator extends AbstractSwingMenuCreator<JMenu> {

	@Override
	protected void addLeafToTop(final ShadowMenu shadow, final JMenu target) {
		addLeafToMenu(shadow, target);
	}

	@Override
	protected JMenu addNonLeafToTop(final ShadowMenu shadow, final JMenu target) {
		return addNonLeafToMenu(shadow, target);
	}

	@Override
	protected void addSeparatorToTop(final JMenu target) {
		addSeparatorToMenu(target);
	}

}
