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

package org.scijava.ui.swing.console;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import net.miginfocom.swing.MigLayout;

import org.scijava.log.LogLevel;
import org.scijava.log.LogMessage;
import org.scijava.log.LogSource;

/**
 * {@link LogSourcesPanel} is a {@link JPanel}, that contains a tree view, where
 * {@link LogSource}s are listed. For each log source the user may select a set
 * of visible log levels.
 *
 * @author Matthias Arzt
 */
class LogSourcesPanel extends JPanel {

	private static final EnumSet<Level> VALID_LEVELS = EnumSet.range(Level.ERROR,
		Level.TRACE);

	private final JPopupMenu menu = new JPopupMenu();

	private final Map<LogSource, Item> sourceItems = new HashMap<>();
	private JTree tree;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Log Sources:");

	private Predicate<LogMessage> filter = message -> true;
	private Runnable changeListener = null;
	private List<LogSource> selected;

	// -- constructor --

	LogSourcesPanel(JButton reloadButton) {
		initMenu();
		initTreeView();
		JButton visibilityButton = initVisibilityButton();
		setLayout(new MigLayout("inset 0", "[grow]", "[][grow][]"));
		add(new JLabel("Log Sources:"), "grow, wrap");
		add(new JScrollPane(tree), "grow, wrap");
		add(new JLabel(""), "split 3, grow");
		add(reloadButton);
		add(visibilityButton);
		actionWhenFocusLost(() -> tree.clearSelection(), Arrays.asList(tree, visibilityButton));
	}

	// -- LogLevelPanel methods --

	public void setChangeListener(Runnable changeListener) {
		this.changeListener = changeListener;
	}

	public Predicate<LogMessage> getFilter() {
		if (filter == null) updateFilter();
		return filter;
	}

	public void updateSources(Set<LogSource> logSources) {
		for (LogSource sources : logSources)
			getItem(sources);
		treeModel.reload();
	}

	// -- Helper methods --

	private void initTreeView() {
		treeModel = new DefaultTreeModel(rootNode);
		tree = new JTree(treeModel);
		tree.setRootVisible(false);
		DefaultTreeCellRenderer cellRenderer = new DefaultTreeCellRenderer();
		cellRenderer.setIcon(null);
		cellRenderer.setLeafIcon(null);
		cellRenderer.setOpenIcon(null);
		tree.setCellRenderer(cellRenderer);
		tree.setComponentPopupMenu(menu);
		tree.setEditable(false);
		tree.setShowsRootHandles(true);
		tree.addTreeSelectionListener(this::selectionChanged);
	}

	private void selectionChanged(TreeSelectionEvent treeSelectionEvent) {
		selected = getSelectedItems().map(item -> item.source).collect(Collectors.toList());
		settingsChanged();
	}

	private JButton initVisibilityButton() {
		JButton button = new JButton("visibility");
		button.addActionListener(a -> menu.show(button, 0, button.getHeight()));
		return button;
	}

	private void initMenu() {
		EnumSet<Level> levels = EnumSet.range(Level.ERROR, Level.TRACE);
		addMenuItemPerLevel(EnumSet.of(Level.TRACE), level -> "show all",
			this::onShowErrorUpToClicked);
		addMenuItemPerLevel(EnumSet.of(Level.NONE), level -> "hide all",
			this::onShowNoneClicked);
		menu.addSeparator();
		addMenuItemPerLevel(levels, level -> "show " + level.toString(),
			this::onShowLogLevelClicked);
		menu.addSeparator();
		addMenuItemPerLevel(levels, level -> "hide " + level.toString(),
			this::onHideLogLevelClicked);
		menu.addSeparator();
		addMenuItemPerLevel(levels, LogSourcesPanel::listLevelsErrorTo,
			this::onShowErrorUpToClicked);
	}

	private void addMenuItemPerLevel(EnumSet<Level> levels,
		Function<Level, String> title, Consumer<Level> consumer)
	{
		for (Level level : levels) {
			JMenuItem menuItem = new JMenuItem(title.apply(level));
			menuItem.addActionListener(a -> consumer.accept(level));
			menu.add(menuItem);
		}
	}

	private void onShowLogLevelClicked(Level level) {
		modifyFilterOnSelection(filter -> {
			filter.add(level);
			return filter;
		});
	}

	private void onHideLogLevelClicked(Level level) {
		modifyFilterOnSelection(filter -> {
			filter.remove(level);
			return filter;
		});
	}

	private void onShowErrorUpToClicked(Level level) {
		EnumSet<Level> enumSet = EnumSet.range(Level.ERROR, level);
		modifyFilterOnSelection(ignore -> enumSet);
	}

	private void onShowNoneClicked(Level level) {
		EnumSet<Level> enumSet = EnumSet.noneOf(Level.class);
		modifyFilterOnSelection(ignore -> enumSet);
	}

	private void modifyFilterOnSelection(
		Function<EnumSet<Level>, EnumSet<Level>> itemConsumer)
	{
		getSelectedItems().forEach(item -> {
			item.setLevelSet(itemConsumer.apply(item.levels));
			treeModel.nodeChanged(item.node);
		});
		settingsChanged();
	}

	private Stream<Item> getSelectedItems() {
		TreePath[] selectionPaths = tree.getSelectionPaths();
		if (selectionPaths == null) return Collections.<Item> emptyList().stream();
		return Stream.of(selectionPaths).map(path -> getItem(
			(DefaultMutableTreeNode) path.getLastPathComponent()));
	}

	private void settingsChanged() {
		filter = null;
		if (changeListener != null) changeListener.run();
	}

	private Item getItem(DefaultMutableTreeNode node) {
		return (Item) node.getUserObject();
	}

	private Item getItem(LogSource source) {
		Item existing = sourceItems.get(source);
		return existing == null ? initItem(source) : existing;
	}

	private Item initItem(LogSource source) {
		Item item = new Item(source);
		sourceItems.put(item.source, item);
		DefaultMutableTreeNode parent = source.isRoot() ?
				rootNode : getItem(source.parent()).node;
		parent.add(item.node);
		return item;
	}

	private void updateFilter() {
		Map<LogSource, EnumSet<Level>> filterData = new HashMap<>();
		EnumSet<Level> none = EnumSet.noneOf(Level.class);
		sourceItems.forEach((name, item) -> filterData.put(name, item.visible
			? item.levels : none));
		Set<LogSource> selectedSources = new HashSet<>(selected);
		filter = message -> {
			if(!selectedSources.isEmpty() && !selectedSources.contains(message.source()))
				return false;
			EnumSet<Level> logLevels = filterData.get(message.source());
			if (logLevels == null) return true;
			return logLevels.contains(Level.of(message.level()));
		};
	}

	private static String listLevelsErrorTo(Level max) {
		return enumSetToString(EnumSet.range(Level.ERROR, max));
	}

	private static String enumSetToString(EnumSet<Level> levels) {
		StringJoiner s = new StringJoiner(", ");
		for (Level level : levels)
			s.add(level.toString());
		return s.toString();
	}

	private static void actionWhenFocusLost(Runnable action, List<JComponent> components) {
		FocusListener l = new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) {
			}

			@Override
			public void focusLost(FocusEvent e) {
				boolean keepSelection = components.contains(e.getOppositeComponent()) || e.isTemporary();
				if (!keepSelection) action.run();
			}
		};
		components.forEach(c -> c.addFocusListener(l));
	}

	// -- Helper classes --

	private static class Item {

		DefaultMutableTreeNode node;
		LogSource source;
		EnumSet<Level> levels;
		boolean visible;

		public Item(LogSource source) {
			this.levels = EnumSet.range(Level.ERROR, Level.TRACE);
			this.source = source;
			this.visible = true;
			this.node = new DefaultMutableTreeNode(this);
		}

		@Override
		public String toString() {
			String name = source.isRoot() ? "ROOT" : source.name();
			return name + " " + getLevelString();
		}

		private String getLevelString() {
			if (levels.equals(VALID_LEVELS)) return "";
			if (levels.isEmpty()) return "[hidden]";
			return levels.toString();
		}

		public void setLevelSet(EnumSet<Level> value) {
			levels = value.clone();
		}
	}

	private enum Level {
			NONE, ERROR, WARN, INFO, DEBUG, TRACE;

		static Level of(int x) {
			switch (x) {
				case LogLevel.NONE:
					return NONE;
				case LogLevel.ERROR:
					return ERROR;
				case LogLevel.WARN:
					return WARN;
				case LogLevel.INFO:
					return INFO;
				case LogLevel.DEBUG:
					return DEBUG;
				default:
					if (x >= LogLevel.TRACE) return TRACE;
					throw new IllegalArgumentException();
			}
		}
	}

	public static void main(String... args) {
		JFrame frame = new JFrame();
		LogSourcesPanel logLevelPanel = new LogSourcesPanel(new JButton("dummy"));
		LogSource root = LogSource.newRoot();
		Set<LogSource> loggers = new HashSet<>(Arrays.asList(
				root.subSource("Hello:World"),
				root.subSource("Hello:Universe"),
				root.subSource("Hello:foo:bar")
		));
		logLevelPanel.updateSources(loggers);
		frame.getContentPane().add(logLevelPanel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}
