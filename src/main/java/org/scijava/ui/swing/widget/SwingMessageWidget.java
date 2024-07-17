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

package org.scijava.ui.swing.widget;

import java.io.IOException;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.scijava.Priority;
import org.scijava.log.LogService;
import org.scijava.platform.PlatformService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.InputWidget;
import org.scijava.widget.MessageWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of message widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class, priority = Priority.HIGH)
public class SwingMessageWidget extends SwingInputWidget<String> implements
	MessageWidget<JPanel>
{
	@Parameter
	private PlatformService platformService;

	@Parameter
	private LogService logService;

	private JEditorPane pane;

	// -- InputWidget methods --

	@Override
	public String getValue() {
		return null;
	}

	@Override
	public boolean isLabeled() {
		final String l = get().getItem().getLabel();
		return l != null && !l.isEmpty();
	}

	@Override
	public boolean isMessage() {
		return true;
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final String text = model.getText();

		pane = new JEditorPane("text/html", text);

		// NB: use format (font etc.) from parent component
		pane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

		pane.setEditable(false);
		pane.setOpaque(false);
		pane.addHyperlinkListener(new HyperlinkListener() {

			@Override
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					try {
						platformService.open(hle.getURL());
					}
					catch (IOException exc) {
						logService.error("Error while opening " + hle.getURL(), exc);
					}
				}
			}
		});
		getComponent().add(pane);
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isMessage();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		// maybe dialog owner changed message content
		pane.setText(get().getText());
	}
}
