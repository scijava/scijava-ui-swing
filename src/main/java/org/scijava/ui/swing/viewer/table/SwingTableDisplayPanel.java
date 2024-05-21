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

package org.scijava.ui.swing.viewer.table;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.scijava.table.Table;
import org.scijava.table.TableDisplay;
import org.scijava.ui.viewer.table.TableDisplayPanel;

import org.scijava.ui.viewer.DisplayWindow;

/**
 * This is the display panel for {@link Table}s.
 * 
 * @author Curtis Rueden
 * @author Barry DeZonia
 */
public class SwingTableDisplayPanel extends JScrollPane implements
	TableDisplayPanel
{

	// -- instance variables --

	private final DisplayWindow window;
	private final TableDisplay display;
	private final JTable table;
	private final NullTableModel nullModel;

	// -- constructor --

	public SwingTableDisplayPanel(final TableDisplay display,
		final DisplayWindow window)
	{
		this.display = display;
		this.window = window;
		nullModel = new NullTableModel();
		table = makeTable();

		table.setAutoCreateRowSorter(true);
		table.setRowSelectionAllowed(true);
		new TablePopupMenu().install();

		//table.setPreferredScrollableViewportSize(getPreferredSize());
		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(final ComponentEvent e) {
				if (table.getPreferredSize().width < getWidth()) {
					table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
				} else {
					table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				}
			}
		});
		setViewportView(table);
		window.setContent(this);

	}

	// -- TableDisplayPanel methods --

	@Override
	public TableDisplay getDisplay() {
		return display;
	}

	// -- DisplayPanel methods --

	@Override
	public DisplayWindow getWindow() {
		return window;
	}

	@Override
	public void redoLayout() {
		// Nothing to layout
	}

	@Override
	public void setLabel(final String s) {
		// The label is not shown.
	}

	@Override
	public void redraw() {
		// BDZ - I found a TODO here saying implement me. Not sure if my one liner
		// is correct but it seems to work.
		// one liner:
		// table.update(table.getGraphics());
		// BDZ now try something more intuitive
		// table.repaint(); // nope
		// BDZ this?
		// table.doLayout(); // nope

		// note that our table is not attached to the table model as a listener.
		// we might need to do that. but try to find a simple way to enforce a
		// reread of the table because when it gets in here it's contents are ok

		javax.swing.table.TableModel model = table.getModel();

		// BDZ attempt to force a rebuild. no luck. and also fails the Clear test.
		// Object v = model.getValueAt(0, 0);
		// model.setValueAt(v, 0, 0);

		// BDZ hacky hack way that works
		table.setModel(nullModel);
		table.setModel(model);
		// table.repaint();
	}

	// -- Helper methods --

	private JTable makeTable() {
		return new JTable(new TableModel(getTable()));
	}

	private Table<?, ?> getTable() {
		return display.size() == 0 ? null : display.get(0);
	}

	// -- Helper classes --

	@SuppressWarnings("serial")
	public static class NullTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 0;
		}

		@Override
		public int getRowCount() {
			return 0;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return null;
		}

	}

	/** A Swing {@link TableModel} backed by an ImageJ {@link Table}. */
	public static class TableModel extends AbstractTableModel {

		private final Table<?, ?> tab;

		public TableModel(final Table<?, ?> table) {
			this.tab = table;
		}

		@Override
		public String getColumnName(final int col) {
			if (col == 0) return "";
			return tab.getColumnHeader(col - 1);
		}

		@Override
		public int getRowCount() {
			return tab.getRowCount();
		}

		@Override
		public int getColumnCount() {
			return tab.getColumnCount() + 1; // +1 for row header column
		}

		@Override
		public Object getValueAt(final int row, final int col) {
			if (row < 0 || row >= getRowCount()) return null;
			if (col < 0 || col >= getColumnCount()) return null;

			if (col == 0) {
				// get row header, or row number if none
				// NB: Assumes the JTable can handle Strings equally as well as the
				// underlying type T of the Table.
				final String header = tab.getRowHeader(row);
				if (header != null) return header;
				return "" + (row + 1);
			}

			// get the underlying table value
			// NB: The column is offset by one to accommodate the row header/number.
			return tab.get(col - 1, row);
		}

		@Override
		public void setValueAt(final Object value, final int row, final int col) {
			if (row < 0 || row >= getRowCount()) return;
			if (col < 0 || col >= getColumnCount()) return;
			if (col == 0) {
				// set row header
				tab.setRowHeader(row, value == null ? null : value.toString());
				return;
			}
			set(tab, col - 1, row, value);
			fireTableCellUpdated(row, col);
		}

		public void removeRows(int[] indices) {
			Arrays.sort(indices);
			for (int i = indices.length - 1; i >= 0; i--) {
				tab.removeRow(indices[i]);
				fireTableRowsDeleted(indices[i], indices[i]);
			}
		}

		// -- Helper methods --

		private <T> void set(final Table<?, T> table,
			final int col, final int row, final Object value)
		{
			@SuppressWarnings("unchecked")
			final T typedValue = (T) value;
			table.set(col, row, typedValue);
		}

	}

	@SuppressWarnings("serial")
	class TablePopupMenu extends JPopupMenu {
		public TablePopupMenu() {
			super();
			JMenuItem mi;
			mi= new JMenuItem(new ActionMapAction("Copy", table, "copy"));
			final int MASK = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
			mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, MASK));
			add(mi);
			mi = new JMenuItem(new ActionMapAction("Select All", table, "selectAll"));
			mi.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, MASK));
			add(mi);
			addSeparator();
			mi = new JMenuItem("Delete Selected Row(s)");
			mi.addActionListener(e -> {
				final int[] selectedRows = table.getSelectedRows();
				if (selectedRows.length > 0)
					((TableModel) table.getModel()).removeRows(selectedRows);
			});
			add(mi);
			mi = new JMenuItem("Resize Column Widths");
			mi.addActionListener(e -> resizeColumns());
			add(mi);
		}

		private void resizeColumns() {
			// https://stackoverflow.com/a/30355804
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			table.getTableHeader();
			for (int column = 0; column < table.getColumnCount(); column++) {
				final TableColumn tableColumn = table.getColumnModel().getColumn(column);
				int preferredWidth = Math.max(tableColumn.getMinWidth(), getColumnHeaderWidth(tableColumn, column));
				final int maxWidth = tableColumn.getMaxWidth();
				for (int row = 0; row < table.getRowCount(); row++) {
					final TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
					final Component c = table.prepareRenderer(cellRenderer, row, column);
					final int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
					preferredWidth = Math.max(preferredWidth, width);
					if (preferredWidth >= maxWidth) {
						preferredWidth = maxWidth;
						break;
					}
				}
				tableColumn.setPreferredWidth(preferredWidth);
			}
		}

		private int getColumnHeaderWidth(final TableColumn tableColumn, final int column) {
			final Object value = tableColumn.getHeaderValue();
			TableCellRenderer renderer = tableColumn.getHeaderRenderer();
			if (renderer == null) {
				renderer = table.getTableHeader().getDefaultRenderer();
			}
			final Component c = renderer.getTableCellRendererComponent(table, value, false, false, -1, column);
			return c.getPreferredSize().width;
		}

		void install() {
			table.setComponentPopupMenu(this);
			SwingTableDisplayPanel.this.setComponentPopupMenu(this);
		}

	}

	@SuppressWarnings("serial")
	private class ActionMapAction extends AbstractAction {

		private final Action originalAction;
		private final JComponent component;
		private final String actionCommand = "";

		ActionMapAction(final String name, final JComponent component, final String actionKey) {
			super(name);
			originalAction = component.getActionMap().get(actionKey);
			if (originalAction == null) {
				throw new IllegalArgumentException("No Action for action key: " + actionKey);
			}
			this.component = component;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			e = new ActionEvent(component, ActionEvent.ACTION_PERFORMED, actionCommand, e.getWhen(), e.getModifiers());
			originalAction.actionPerformed(e);
		}

	}
}
