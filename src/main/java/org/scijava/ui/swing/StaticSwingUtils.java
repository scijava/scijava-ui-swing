/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2015 Board of Regents of the University of
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

/*
Adapted from Superliminal Software's Static Utilities collection:
  http://www.superliminal.com/sources/sources.htm#Java%20Code

The Superliminal Software web site states:
"You're completely free to use them for any purpose whatsoever."
*/

package org.scijava.ui.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BoundedRangeModel;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

/**
 * A collection of generally useful Swing utility methods.
 *
 * @author Melinda Green
 */
public class StaticSwingUtils {

	private StaticSwingUtils() {
		// NB: Prevent instantiation of utility class.
	}

	public static JButton createButton16(final ImageIcon icon) {
		return createButton16(icon, null);
	}

	public static JButton createButton16(final ImageIcon icon,
		final String toolTip)
	{
		final JButton newButton = new JButton();
		newButton.setMargin(new Insets(0, 0, 0, 0));
		newButton.setMinimumSize(new Dimension(16, 16));
		if (toolTip != null) {
			newButton.setToolTipText(toolTip);
		}
		newButton.setIcon(icon);
		return newButton;
	}

	/**
	 * Adds a control hot key to the containing window of a component. In the case
	 * of buttons and menu items it also attaches the given action to the
	 * component itself.
	 *
	 * @param key one of the KeyEvent keyboard constants
	 * @param to component to map to
	 * @param actionName unique action name for the component's action map
	 * @param action callback to notify when control key is pressed
	 */
	public static void addHotKey(final int key, final JComponent to,
		final String actionName, final Action action)
	{
		final KeyStroke keystroke =
			KeyStroke.getKeyStroke(key, java.awt.event.InputEvent.CTRL_MASK);
		final InputMap map = to.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		map.put(keystroke, actionName);
		to.getActionMap().put(actionName, action);
		if (to instanceof JMenuItem) {
			((JMenuItem) to).setAccelerator(keystroke);
		}
		if (to instanceof AbstractButton) // / includes JMenuItem/
		{
			((AbstractButton) to).addActionListener(action);
		}
	}

	/**
	 * Finds the top-level JFrame in the component tree containing a given
	 * component.
	 *
	 * @param comp leaf component to search up from
	 * @return the containing JFrame or null if none
	 */
	public static JFrame getTopFrame(final Component comp) {
		if (comp == null) return null;
		Component top = comp;
		while (top.getParent() != null) {
			top = top.getParent();
		}
		if (top instanceof JFrame) return (JFrame) top;
		return null;
	}

	/**
	 * Different platforms use different mouse gestures as pop-up triggers. This
	 * class unifies them. Just implement the abstract popUp method to add your
	 * handler.
	 */
	public static abstract class PopperUpper extends MouseAdapter {

		// To work properly on all platforms, must check on mouse press as well as
		// release

		@Override
		public void mousePressed(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				popUp(e);
			}
		}

		@Override
		public void mouseReleased(final MouseEvent e) {
			if (e.isPopupTrigger()) {
				popUp(e);
			}
		}

		protected abstract void popUp(MouseEvent e);
	}

	// / simple Clipboard string routines/

	public static void placeInClipboard(final String str) {
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
			new StringSelection(str), null);
	}

	public static String getFromClipboard() {
		String str = null;
		try {
			str =
				(String) Toolkit.getDefaultToolkit().getSystemClipboard().getContents(
					null).getTransferData(DataFlavor.stringFlavor);
		}
		catch (final UnsupportedFlavorException e) {
			e.printStackTrace();
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		return str;
	}

	// --- Ultimate Parent ---------------------------------------------
	// Find ultimate parent window, even if in a popup
	// Possible usage:
	// public void actionPerformed (ActionEvent evt) {
	// Component c = findUltimateParent((Component) evt.getSource());
	// NEEDS TESTING

	public Component findUltimateParent(final Component c) {
		Component parent = c;
		while (null != parent.getParent()) {
			parent = parent.getParent();
			if (parent instanceof JPopupMenu) {
				final JPopupMenu popup = (JPopupMenu) parent;
				parent = popup.getInvoker();
			}
		}
		return parent;
	}

	// == Diagnostics =======================================================

	private static final int DEFAULT_X = 5;
	private static final int DEFAULT_Y = 80;

	private static Point lastFramePosition = new Point(DEFAULT_X, DEFAULT_Y);

	public static void locateCenter(final Container component) {
		final int w = component.getWidth();
		final int h = component.getHeight();
		final Rectangle bounds = getWorkSpaceBounds();
		final int x = (int) (bounds.getX() + (bounds.getWidth() - w) / 2);
		final int y = (int) (bounds.getY() + (bounds.getHeight() - h) / 2);
		component.setLocation(x, y);
	}

	public static void locateUpperRight(final Container component) {
		final int w = component.getWidth();
		final Rectangle bounds = getWorkSpaceBounds();
		final int x = (int) (bounds.getX() + bounds.getWidth() - w);
		final int y = (int) (bounds.getY());
		component.setLocation(x, y);
	}

	public static void locateLowerRight(final Container component) {
		final int w = component.getWidth();
		final int h = component.getHeight();
		final Rectangle bounds = getWorkSpaceBounds();
		final int x = (int) (bounds.getX() + bounds.getWidth() - w);
		final int y = (int) (bounds.getY() + bounds.getHeight() - h);
		component.setLocation(x, y);
	}

	public static void locateUpperLeft(final Container component) {
		final Rectangle bounds = getWorkSpaceBounds();
		component.setLocation((int) bounds.getX(), (int) bounds.getY());
	}

	public static void locateLowerLeft(final Container component) {
		final int h = component.getHeight();
		final Rectangle bounds = getWorkSpaceBounds();
		final int x = (int) (bounds.getX());
		final int y = (int) (bounds.getY() + bounds.getHeight() - h);
		component.setLocation(x, y);
	}

	public static Point nextFramePosition() {
		lastFramePosition.x = lastFramePosition.x + 16;
		lastFramePosition.y = lastFramePosition.y + 16;
		if (lastFramePosition.x > 200) {
			lastFramePosition.x = DEFAULT_X;
			lastFramePosition.y = DEFAULT_Y;
		}
		return lastFramePosition;
	}

	public static Rectangle getWorkSpaceBounds() {
		// @todo deal with default when multi-monitor
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
			.getMaximumWindowBounds();
	}

	/** @deprecated Use {@link org.scijava.thread.ThreadService#queue}. */
	@Deprecated
	public static void dispatchToEDT(final Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(runnable);
		}
		else {
			runnable.run();
		}
	}

	/** @deprecated Use {@link org.scijava.thread.ThreadService#invoke}. */
	@Deprecated
	public static void dispatchToEDTWait(final Runnable runnable) {
		if (!SwingUtilities.isEventDispatchThread()) {
			try {
				SwingUtilities.invokeAndWait(runnable);
			}
			catch (final InterruptedException ex) {
				Logger.getLogger(StaticSwingUtils.class.getName()).log(Level.SEVERE,
					null, ex);
			}
			catch (final InvocationTargetException ex) {
				Logger.getLogger(StaticSwingUtils.class.getName()).log(Level.SEVERE,
					null, ex);
			}
		}
		else {
			runnable.run();
		}
	}

	// -- Utility methods - scrolling --

	/** Scrolls the given {@link JScrollPane} to its bottom left corner. */
	public static void scrollToBottom(final JScrollPane scrollPane) {
		final JViewport viewport = scrollPane.getViewport();
		viewport.setViewPosition(new Point(0, viewport.getView().getHeight()));
	}

	/**
	 * Checks whether the given {@link JScrollPane} is currently scrolled to the
	 * bottom of its view.
	 */
	public static boolean isScrolledToBottom(final JScrollPane scrollPane) {
		final BoundedRangeModel m = scrollPane.getVerticalScrollBar().getModel();
		return m.getValue() + m.getExtent() >= m.getMaximum();
	}

}
