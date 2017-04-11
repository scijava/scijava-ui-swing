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

package org.scijava.ui.swing.widget;

import java.util.List;
import java.util.Vector;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.scijava.Priority;
import org.scijava.convert.ConvertService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.util.IntArray;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of multiple selection widget using a {@link JList}.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class, priority = SwingMultiSelectWidget.PRIORITY)
public class SwingMultiSelectWidget<E> extends SwingInputWidget<List<E>>
	implements ListSelectionListener, MultiSelectWidget<E, JPanel>
{

	public static final double PRIORITY = Priority.NORMAL_PRIORITY;

	private JList<E> listBox;

	@Parameter
	private ConvertService convertService;

	private List<E> choices;

	// -- ListSelectionListener methods --

	@Override
	public void valueChanged(ListSelectionEvent e) {
		updateModel();
	}

	// -- InputWidget methods --

	@Override
	public List<E> getValue() {
		return listBox.getSelectedValuesList();
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		final String[] items = model.getChoices();
		@SuppressWarnings("unchecked")
		final List<E> list = (List<E>) //
			convertService.convert(items, model.getItem().getGenericType());
		choices = list;

		// NB: JList has only JList(Vector) constructor, no JList(List).
		listBox = new JList<>(new Vector<>(list));

		setToolTip(listBox);
		getComponent().add(listBox);
		listBox.addListSelectionListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && isList(model) && model.isMultipleChoice();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		@SuppressWarnings("unchecked")
		final List<E> value = (List<E>) get().getValue();
		if (value.equals(getValue())) return; // no change

		listBox.setSelectedIndices(indices(value));
	}

	// -- Helper methods --

	private boolean isList(final WidgetModel model) {
		return List.class.isAssignableFrom(model.getItem().getType());
	}

	private int[] indices(List<E> list) {
		final IntArray indices = new IntArray();
		for (final E item : list) {
			int index = choices.indexOf(item);
			if (index >= 0) indices.add(index);
		}
		return indices.copyArray();
	}
}
