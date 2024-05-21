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

package org.scijava.ui.swing.viewer;

import java.awt.HeadlessException;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import org.scijava.ui.swing.StaticSwingUtils;
import org.scijava.ui.viewer.DisplayPanel;
import org.scijava.ui.viewer.DisplayWindow;

/**
 * Swing class implementation of the {@link DisplayWindow} interface.
 * 
 * @author Grant Harris
 * @author Barry DeZonia
 */
public class SwingDisplayWindow extends JFrame implements DisplayWindow {

	private JComponent panel;

	public SwingDisplayWindow() throws HeadlessException {
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocation(StaticSwingUtils.nextFramePosition());
	}

	// -- DisplayWindow methods --

	@Override
	public void setContent(final DisplayPanel panel) {
		// TODO - eliminate hacky cast
		this.panel = (JComponent) panel;
		setContentPane(this.panel);
	}

	@Override
	public void showDisplay(final boolean visible) {
		if (visible) pack();
		setVisible(visible);
	}

	@Override
	public void close() {
		setVisible(false);
		dispose();
	}
	
	@Override
	public int findDisplayContentScreenX() {
		// FIXME: Implement this properly.
		return 0;
	}

	@Override
	public int findDisplayContentScreenY() {
		// FIXME: Implement this properly.
		return 0;
	}

//	// TODO - BDZ - this is a bit hacky and will fail if we go away from
//	//        JHotDrawImageCanvas
//	@Override
//	public int findDisplayContentScreenX() {
//		JHotDrawImageCanvas canvas = findCanvas(getContentPane());
//		if (canvas == null)
//			 throw new IllegalArgumentException("Cannot find JHotDrawImageCanvas");
//		return canvas.getLocationOnScreen().x;
//	}
//
//	// TODO - BDZ - this is a bit hacky and will fail if we go away from
//	//        JHotDrawImageCanvas
//	@Override
//	public int findDisplayContentScreenY() {
//		JHotDrawImageCanvas canvas = findCanvas(getContentPane());
//		if (canvas == null)
//			 throw new IllegalArgumentException("Cannot find JHotDrawImageCanvas");
//		return canvas.getLocationOnScreen().y;
//	}
//
//	// -- private helpers --
//	
//	private JHotDrawImageCanvas findCanvas(Component c) {
//		if (c instanceof JHotDrawImageCanvas) {
//			return (JHotDrawImageCanvas) c;
//		}
//		if (c instanceof Container) {
//			Container container = (Container) c;
//			for (Component comp : container.getComponents()) {
//				JHotDrawImageCanvas canvas = findCanvas(comp);
//				if (canvas != null) return canvas;
//			}
//		}
//		return null;
//	}
	
}
