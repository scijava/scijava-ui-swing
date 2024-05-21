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

package org.scijava.ui.swing.commands;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.scijava.MenuEntry;
import org.scijava.MenuPath;
import org.scijava.log.LogService;
import org.scijava.module.ModuleInfo;
import org.scijava.module.ModuleService;
import org.scijava.util.FileUtils;
import org.scijava.util.Types;

/**
 * A panel that allows the user to search for SciJava commands. Based on the
 * original Command Finder plugin by Mark Longair and Johannes Schindelin.
 * 
 * @author Curtis Rueden
 * @author Johannes Schindelin
 */
public class CommandFinderPanel extends JPanel implements ActionListener,
	DocumentListener
{

	protected final JTextField searchField;
	protected final JTable commandsList;
	protected final CommandTableModel tableModel;

	private final LogService log;
	private final List<ModuleInfo> commands;

	public CommandFinderPanel(final ModuleService moduleService,
		final String baseDir)
	{
		log = moduleService.context().getService(LogService.class);
		commands = buildCommands(moduleService);

		setPreferredSize(new Dimension(800, 600));

		searchField = new JTextField(12);
		commandsList = new JTable(20, CommandTableModel.COLUMN_COUNT);
		commandsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		commandsList.setRowSelectionAllowed(true);
		commandsList.setColumnSelectionAllowed(false);
		commandsList.setAutoCreateRowSorter(true);

		commandsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() < 2) return;
				int row = commandsList.rowAtPoint(e.getPoint());
				if (row >= 0) {
					closeDialog(true);
				}
			}
		});
		commandsList.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					e.consume();
					closeDialog(true);
				}
			}
		});

		searchField.getDocument().addDocumentListener(this);

		tableModel = new CommandTableModel(commands, baseDir);
		commandsList.setModel(tableModel);
		tableModel.setColumnWidths(commandsList.getColumnModel());

		final String layout = "fillx,wrap 2";
		final String cols = "[pref|fill,grow]";
		final String rows = "[pref|fill,grow|pref]";
		setLayout(new MigLayout(layout, cols, rows));
		add(new JLabel("Type part of a command:"));
		add(searchField);
		add(new JScrollPane(commandsList), "grow,span 2");

		searchField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						commandsList.scrollRectToVisible(commandsList.getCellRect(0, 0, true));
						commandsList.setRowSelectionInterval(0, 0);
						commandsList.grabFocus();
						break;
					case KeyEvent.VK_UP:
						final int index = commandsList.getModel().getRowCount() - 1;
						commandsList.scrollRectToVisible(commandsList.getCellRect(index, 0, true));
						commandsList.setRowSelectionInterval(index, index);
						commandsList.grabFocus();
						break;
					default:
						return;
				}
				e.consume();
			}
		});

		commandsList.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(final KeyEvent e) {
				int selected;

				switch (e.getKeyCode()) {
					case KeyEvent.VK_DOWN:
						selected = commandsList.getSelectedRow();
						if (selected != commandsList.getModel().getRowCount() - 1) return;
						searchField.grabFocus();
						break;
					case KeyEvent.VK_UP:
						selected = commandsList.getSelectedRow();
						if (selected != 0) return;
						searchField.grabFocus();
						break;
					default:
						return;
				}
				e.consume();
			}
		});
	}

	// -- CommandFinderPanel methods --

	/** Gets the currently selected command. */
	public ModuleInfo getCommand() {
		if (tableModel.getRowCount() < 1) return null;
		int selectedRow = commandsList.getSelectedRow();
		if (selectedRow < 0) selectedRow = 0;
		return tableModel.get(commandsList.convertRowIndexToModel(selectedRow));
	}

	/** Gets the {@link JTextField} component for specifying the search string. */
	public JTextField getSearchField() {
		return searchField;
	}

	public String getRegex() {
		return ".*" + searchField.getText().toLowerCase() + ".*";
	}

	private void closeDialog(boolean okay) {
		for (Container parent = getParent(); parent != null; parent = parent.getParent()) {
			if (parent instanceof JOptionPane) {
				((JOptionPane)parent).setValue(okay ? JOptionPane.OK_OPTION : JOptionPane.CANCEL_OPTION);
			} else if (parent instanceof JDialog) {
				((JDialog)parent).setVisible(false);
				break;
			}
		}
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		updateCommands();
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	@Override
	public void insertUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	@Override
	public void removeUpdate(final DocumentEvent arg0) {
		filterUpdated();
	}

	// -- Helper methods --

	/** Builds the master list of available commands. */
	private List<ModuleInfo> buildCommands(final ModuleService moduleService) {
		final List<ModuleInfo> list = new ArrayList<>();
		list.addAll(moduleService.getModules());
		Collections.sort(list);
		return list;
	}

	/** Updates the list of visible commands. */
	private void updateCommands() {
		final ModuleInfo selected = commandsList.getSelectedRow() < 0 ? null : getCommand();
		int counter = 0, selectedRow = -1;
		final String regex = getRegex();
		final List<ModuleInfo> matches = new ArrayList<>();
		for (final ModuleInfo command : commands) {
			if (!command.getMenuPath().toString().toLowerCase().matches(regex)) continue; // no match
			matches.add(command);
			if (command == selected) selectedRow = counter;
			counter++;
		}
		tableModel.setData(matches);
		if (selectedRow >= 0) {
			commandsList.setRowSelectionInterval(selectedRow, selectedRow);
		}
	}

	/** Called when the search filter text field changes. */
	private void filterUpdated() {
		updateCommands();
	}

	// -- Helper classes --

	protected class CommandTableModel extends AbstractTableModel {
		public final static int COLUMN_COUNT = 8;

		private final String baseDir;
		private List<ModuleInfo> list;

		public CommandTableModel(final List<ModuleInfo> list, final String baseDir) {
			this.list = list;
			this.baseDir = baseDir;
		}

		public void setData(List<ModuleInfo> list) {
			this.list = list;
			fireTableDataChanged();
		}

		public void setColumnWidths(TableColumnModel columnModel) {
			int[] widths = { 32, 250, 150, 150, 250, 200, 50, 20 };
			for (int i = 0; i < widths.length; i++) {
				columnModel.getColumn(i).setPreferredWidth(widths[i]);
			}
			final TableColumn iconColumn = columnModel.getColumn(0);
			iconColumn.setMaxWidth(32);
			iconColumn.setCellRenderer(new DefaultTableCellRenderer() {
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					return (Component)value;
				}
			});
		}

		public ModuleInfo get(int index) {
			return list.get(index);
		}

		@Override
		public int getColumnCount() {
			return COLUMN_COUNT;
		}

		@Override
		public String getColumnName(int column) {
			if (column == 0) return "Icon";
			if (column == 1) return "Command";
			if (column == 2) return "Menu Path";
			if (column == 3) return "Shortcut";
			if (column == 4) return "Class";
			if (column == 5) return "File";
			if (column == 6) return "Description";
			if (column == 7) return "Priority";
			return null;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public Object getValueAt(int row, int column) {
			final ModuleInfo info = list.get(row);
			if (column == 0) {
				final String iconPath = info.getIconPath();
				if (iconPath == null) return null;
				final URL iconURL = getClass().getResource(iconPath);
				return iconURL == null ? null : new JLabel(new ImageIcon(iconURL));
			}
			if (column == 1) return info.getTitle();
			if (column == 2) {
				final MenuPath menuPath = info.getMenuPath();
				return menuPath == null ? "" : menuPath.getMenuString(false);
			}
			if (column == 3) {
				final MenuPath menuPath = info.getMenuPath();
				final MenuEntry menuLeaf = menuPath == null ? null : menuPath.getLeaf();
				return menuLeaf == null ? "" : menuLeaf.getAccelerator();
			}
			if (column == 4) return info.getDelegateClassName();
			if (column == 5) {
				Class<?> c = null;
				try {
					c = info.loadDelegateClass();
				}
				catch (final ClassNotFoundException exc) {
					log.warn(exc);
				}
				final URL location = Types.location(c);

				final File file = FileUtils.urlToFile(location);
				final String path = file == null ? null : file.getAbsolutePath();
				if (path != null && path.startsWith(baseDir)) {
					return path.substring(baseDir.length() + 1);
				}
				return file;
			}
			if (column == 6) return info.getDescription();
			if (column == 7) return info.getPriority();
			return null;
		}
	}

}
