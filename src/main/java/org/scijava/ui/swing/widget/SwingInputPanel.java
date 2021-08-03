/*
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

package org.scijava.ui.swing.widget;

import java.awt.Component;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

import org.scijava.ItemVisibility;
import org.scijava.widget.AbstractInputPanel;
import org.scijava.widget.InputPanel;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Swing implementation of {@link InputPanel}.
 * 
 * @author Curtis Rueden
 * @author Karl Duderstadt
 */
public class SwingInputPanel extends AbstractInputPanel<JPanel, JPanel> {

	private JPanel uiComponent;
	
	private Map<String, List<Component>> widgetGroups;
	private Map<String, Boolean> widgetGroupVisible;

	// -- InputPanel methods --

	@Override
	public void addWidget(final InputWidget<?, JPanel> widget) {
		super.addWidget(widget);
		final JPanel widgetPane = widget.getComponent();
		final WidgetModel model = widget.get();
		final String group =  (model.getItem().getVisibility() == ItemVisibility.GROUP) ? (String) model.getValue() : model.getGroup();
		
		if (widgetGroups == null)
			widgetGroups = new HashMap<String, List<Component>>();
		
		if (widgetGroupVisible == null)
			widgetGroupVisible = new HashMap<String, Boolean>();
		
		if (!widgetGroups.containsKey(group)) 
			widgetGroups.put(group, new ArrayList<Component>());
		
		// add widget to panel
		if (model.getItem().getVisibility() == ItemVisibility.GROUP) {
			   JPanel labelPanel = new JPanel(new MigLayout("fillx,insets 5 15 5 15, gapy 0"));
	           JLabel label = (model.getItem().isExpanded()) ? new JLabel("<html><strong>▼ " + group + "</strong></html>") :
	        	   new JLabel("<html><strong>▶ " + group + "</strong></html>");
	           
	           widgetGroupVisible.put(group, model.getItem().isExpanded());

	           label.addMouseListener(new MouseAdapter() {
	               /**
	                * Invoked when the mouse button has been clicked (pressed
	                * and released) on a component.
	                * @param e the event to be processed
	                */
	               @Override
	               public void mouseClicked(MouseEvent e) {
	               }

	               /**
	                * Invoked when a mouse button has been pressed on a component.
	                * @param e the event to be processed
	                */
	               @Override
	               public void mousePressed(MouseEvent e) {
                       widgetGroupVisible.put(group, !widgetGroupVisible.get(group));
            		   widgetGroups.get(group).forEach(comp -> comp.setVisible(widgetGroupVisible.get(group)));

                       if(widgetGroupVisible.get(group))
                    	   label.setText("<html><strong>▼ " + group + "</strong></html>");
                       else
                    	   label.setText("<html><strong>▶ " + group + "</strong></html>");
                       
                       getComponent().revalidate();
	               }

	               /**
	                * Invoked when a mouse button has been released on a component.
	                * @param e the event to be processed
	                */
	               @Override
	               public void mouseReleased(MouseEvent e) {
	               }

	               /**
	                * Invoked when the mouse enters a component.
	                * @param e the event to be processed
	                */
	               @Override
	               public void mouseEntered(MouseEvent e) {
	               }

	               /**
	                * Invoked when the mouse exits a component.
	                * @param e the event to be processed
	                */
	               @Override
	               public void mouseExited(MouseEvent e) {
	               }

	           });

	           labelPanel.add(label);
	           getComponent().add(labelPanel, "align left, wrap");
		}
		else if (widget.isLabeled()) {
			// widget is prefixed by a label
			final JLabel l = new JLabel(model.getWidgetLabel());
			final String desc = model.getItem().getDescription();
			if (desc != null && !desc.isEmpty()) l.setToolTipText(desc);
			getComponent().add(l, "hidemode 3");
			widgetGroups.get(group).add(l);
			
			getComponent().add(widgetPane, "hidemode 3");
			widgetGroups.get(group).add(widgetPane);
		}
		else {
			// widget occupies entire row
			getComponent().add(widgetPane, "span, hidemode 3");
			widgetGroups.get(group).add(widgetPane);
		}
		
		//Make sure components have correct starting visibility
		if (widgetGroups.containsKey(group) && widgetGroupVisible.containsKey(group))
			widgetGroups.get(group).forEach(comp -> comp.setVisible(widgetGroupVisible.get(group)));
	}

	@Override
	public Class<JPanel> getWidgetComponentType() {
		return JPanel.class;
	}

	// -- UIComponent methods --

	@Override
	public JPanel getComponent() {
		if (uiComponent == null) {
			uiComponent = new JPanel();
			final MigLayout layout =
					new MigLayout("fillx,wrap 2", "[right]10[fill,grow]");
			uiComponent.setLayout(layout);
		}
		return uiComponent;
	}

	@Override
	public Class<JPanel> getComponentType() {
		return JPanel.class;
	}

}
