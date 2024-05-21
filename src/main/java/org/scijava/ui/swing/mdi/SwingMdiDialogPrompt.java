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

package org.scijava.ui.swing.mdi;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UserInterface;
import org.scijava.ui.swing.SwingApplicationFrame;

/**
 * TODO
 * 
 * @author Grant Harris
 */
public class SwingMdiDialogPrompt implements DialogPrompt {

	JInternalFrame dialog;
	JOptionPane pane;

	public SwingMdiDialogPrompt(final UserInterface ui, final String message,
		final String title, final MessageType messageType,
		final OptionType optionType)
	{
		final SwingApplicationFrame appFrame =
			(SwingApplicationFrame) ui.getApplicationFrame();
		final JMDIDesktopPane desk = (JMDIDesktopPane) ui.getDesktop();
		pane =
			new JOptionPane(message, msgMap.get(messageType), optionMap
				.get(optionType));
		dialog = new ModalInternalFrame(title, appFrame.getRootPane(), desk, pane);
	}

	@Override
	public Result prompt() {
		dialog.setVisible(true);
		final Object value = pane.getValue();
		dialog.dispose();
		return resultMap.get(value);
	}

	// / Translate DialogPrompt types and results to JOptionPane types and
	// results.
	static final Map<DialogPrompt.MessageType, Integer> msgMap = new HashMap<>();
	static final Map<DialogPrompt.OptionType, Integer> optionMap =
		new HashMap<>();
	static final Map<Integer, DialogPrompt.Result> resultMap = new HashMap<>();

	static {
		msgMap.put(DialogPrompt.MessageType.ERROR_MESSAGE,
			JOptionPane.ERROR_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.INFORMATION_MESSAGE,
			JOptionPane.INFORMATION_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.PLAIN_MESSAGE,
			JOptionPane.PLAIN_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.WARNING_MESSAGE,
			JOptionPane.WARNING_MESSAGE);
		msgMap.put(DialogPrompt.MessageType.QUESTION_MESSAGE,
			JOptionPane.QUESTION_MESSAGE);
		//
		optionMap.put(DialogPrompt.OptionType.DEFAULT_OPTION,
			JOptionPane.DEFAULT_OPTION);
		optionMap.put(DialogPrompt.OptionType.OK_CANCEL_OPTION,
			JOptionPane.OK_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_CANCEL_OPTION,
			JOptionPane.YES_NO_CANCEL_OPTION);
		optionMap.put(DialogPrompt.OptionType.YES_NO_OPTION,
			JOptionPane.YES_NO_OPTION);
		//
		resultMap.put(JOptionPane.CANCEL_OPTION, DialogPrompt.Result.CANCEL_OPTION);
		resultMap.put(JOptionPane.CLOSED_OPTION, DialogPrompt.Result.CLOSED_OPTION);
		resultMap.put(JOptionPane.NO_OPTION, DialogPrompt.Result.NO_OPTION);
		resultMap.put(JOptionPane.OK_OPTION, DialogPrompt.Result.OK_OPTION);
		resultMap.put(JOptionPane.YES_OPTION, DialogPrompt.Result.YES_OPTION);
	}

//	public static JInternalFrame getInternalInputDialog(Component parentComponent, 
//			Object message, String title, int messageType,
//			Icon icon, Object[] selectionValues, Object initialSelectionValue) {
//		JOptionPane pane = new JOptionPane(message, messageType, JOptionPane.OK_CANCEL_OPTION, icon, null, null);
//		Component fo = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
//		pane.setWantsInput(true);
//		pane.setSelectionValues(selectionValues);
//		pane.setInitialSelectionValue(initialSelectionValue);
//		JInternalFrame dialog = pane.createInternalFrame(parentComponent, title);
//		pane.selectInitialValue();
//		dialog.setVisible(true);
//		return dialog;
//	}
}
