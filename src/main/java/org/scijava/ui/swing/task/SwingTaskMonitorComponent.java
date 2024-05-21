/*-
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
package org.scijava.ui.swing.task;

import net.miginfocom.swing.MigLayout;
import org.scijava.Context;
import org.scijava.event.EventHandler;
import org.scijava.task.Task;
import org.scijava.task.event.TaskEvent;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicProgressBarUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;

/**
 * A Swing component with a circular progress bar displaying the overall
 * progress of all registered {@link Task} created via the
 * {@link org.scijava.task.TaskService}.
 * <p>
 * When the user clicks on the circular progress bar, a frame containing a table
 * summarizing each task progression is displayed.
 * </p>
 * <p>
 * Each task is rendered in the table with a custom renderer (see
 * {@link TaskRenderer}). First column:
 * </p>
 * <ul>
 * <li>a label with the name of the task and its status</li>
 * <li>a linear progress bar</li>
 * <li>if enabled in the constructor, a label with an estimation of the
 * remaining time until task completion, the estimation assumes a constant
 * completion speed since the start of the task registration</li>
 * </ul>
 * <p>
 * Second column:
 * </p>
 * <ul>
 * <li>a stop icon, clickable and which calls {@link Task#cancel(String)} for
 * the displayed task</li>
 * </ul>
 * <p>
 * Before cancellation, a user confirmation dialog can be enabled with
 * {@link SwingTaskMonitorComponent#enableCancelConfirmation()} or disabled with
 * {@link SwingTaskMonitorComponent#disableCancelConfirmation()}.
 * </p>
 *
 * @author Nicolas Chiaruttini, EPFL, 2022
 */

public class SwingTaskMonitorComponent {

	/**
	 * Progress bar showing the global progression ( = progression of all tasks ).
	 * Clicking it toggles taskFrame visibility.
	 */
	private final JProgressBar globalProgressBar;
	private final int sizeGlobalProgressBar;

	/**
	 * A container for the {@link #taskTable}, visibility toggled by clickable
	 * {@link #globalProgressBar}.
	 */
	private final JFrame taskFrame;

	/**
	 * {@link JTable} rendering each monitored task, contained in the
	 * {@link #taskFrame}.
	 */
	private final JTable taskTable;

	/** Model of the {@link #taskTable}. */
	private final TaskTableModel taskTableModel;

	/** Flags whether each task should be timed. */
	private final Boolean estimateTimeLeft;

	/**
	 * Flags whether the user should confirm when a task is clicked to be
	 * canceled. not final because this behaviour can be changed.
	 */
	private Boolean confirmBeforeCancel;

	/**
	 * Stores temporarily the current global progression - all tasks have an equal
	 * weight.
	 */
	private double globalProgression = 0;

	/**
	 * Constructs a Swing Task Monitor component - clickable circular progress bar
	 * the component can be accessed with
	 * {@link SwingTaskMonitorComponent#getComponent()}.
	 *
	 * @param context SciJava context
	 * @param estimateTimeLeft whether registered tasks should display an
	 *          estimated remaining time
	 * @param confirmBeforeCancel flags whether a confirmation window should pop up
	 *          when canceling a task, can be overridden with
	 *          {@link SwingTaskMonitorComponent#disableCancelConfirmation()} and
	 *          {@link SwingTaskMonitorComponent#enableCancelConfirmation()}
	 * @param size of the circular progress bar (preferred size)
	 * @param undecorated defines whether taskFrame is undecorated or not
	 */
	public SwingTaskMonitorComponent(final Context context,
		boolean estimateTimeLeft,
		boolean confirmBeforeCancel,
		int size,
		boolean undecorated) {
		context.inject(this); // register event handler (this#onEvent)

		this.sizeGlobalProgressBar = size;
		this.confirmBeforeCancel = confirmBeforeCancel;
		this.estimateTimeLeft = estimateTimeLeft;

		// The global progress bar
		globalProgressBar = new JProgressBar();
		globalProgressBar.setPreferredSize(new Dimension(sizeGlobalProgressBar, sizeGlobalProgressBar));
		globalProgressBar.setUI(new ProgressCircleUI()); // circular
		globalProgressBar.setPreferredSize(new Dimension(sizeGlobalProgressBar, sizeGlobalProgressBar));
		globalProgressBar.setMaximum(100);

		// taskFrame becomes visible when globalProgressBar is clicked
		globalProgressBar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				taskFrame.setVisible(!taskFrame.isVisible());
				if (undecorated) {
					// sets taskFrame location : just right to the globalProgressBar
					taskFrame.setLocation(
						globalProgressBar.getLocationOnScreen().x + globalProgressBar.getWidth(),
						globalProgressBar.getLocationOnScreen().y);
				}
			}
		});

		// "popup" taskFrame, visibility controlled by globalProgressBar
		taskFrame = new JFrame("Tasks");
		if (undecorated) {
			taskFrame.setUndecorated(true);
			taskFrame.setBackground(new Color(1.0f, 1.0f, 1.0f, 0.0f));
		}

		// taskTable showing all tasks, contained in taskFrame
		taskTableModel = new TaskTableModel();
		taskTable = new JTable(taskTableModel);
		taskTable.setShowGrid(false);
		taskTable.setIntercellSpacing(new Dimension(0, 0));
		taskTable.setTableHeader(null); // no header
		taskTable.setRowHeight(65);
		taskTable.setRowMargin(2);
		taskTable.setDefaultRenderer(Task.class, new TaskRenderer(false));
		// restrict size of second column to the size of the stop icon
		taskTable.getColumnModel().getColumn(1).setMaxWidth(30);

		// Scroll pane containing the JTable -> necessary when many tasks are displayed
		JScrollPane scrollPane = new JScrollPane(taskTable);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		taskFrame.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setPreferredSize(new Dimension(200,265));
		scrollPane.setColumnHeaderView(null);
		taskFrame.pack();

		// enable canceling when the user clicks on the second column (of index 1)
		taskTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {
				int row = taskTable.rowAtPoint(evt.getPoint());
				int col = taskTable.columnAtPoint(evt.getPoint());
				if (row >= 0 && col == 1) {
					Task selectedTask = taskTableModel.getTask(row);
					if (selectedTask!=null) {
						if (SwingTaskMonitorComponent.this.confirmBeforeCancel) {
							int userconfirmation = JOptionPane.showConfirmDialog(null, "Do you really want to cancel this task ?", "Canceling " + selectedTask.getName(), JOptionPane.YES_NO_OPTION);
							if (userconfirmation == JOptionPane.YES_OPTION) {
								selectedTask.cancel("User cancellation (table task)");
							}
						} else {
							selectedTask.cancel("User cancellation (table task)");
						}
					}
				}
			}
		});
	}

	/**
	 * Retrieves the global clickable progress bar that can be placed in the interface
	 * @return the component that needs to be placed in the interface to gives users a view to the current running tasks
	 */
	public JComponent getComponent() {
		return globalProgressBar;
	}

	/*
	 * Catch all task events and updates the task table model accordingly - registered thanks to context injection in the constructor
	 */
	@EventHandler
	private void onEvent(final TaskEvent evt) {
		Task task = evt.getTask();
		if (task.isDone()) {
			taskTableModel.removeTask(task);
		} else {
			taskTableModel.addOrUpdate(task);
		}
		// globalProgression has been updated during taskTableModel update
		globalProgressBar.setValue((int)(globalProgression*100));
	}

	/**
	 * User confirmation required when canceling a task by clicking the task table.
	 */
	public void enableCancelConfirmation() {
		this.confirmBeforeCancel = true;
	}

	/**
	 * NO user confirmation required when canceling a task by clicking the task table.
	 */
	public void disableCancelConfirmation() {
		this.confirmBeforeCancel = false;
	}

	/**
	 * Task Table Model, serves to update the table according to the events
	 * received. Note that nothing is synchronized because every call is expected
	 * to happen from the event dispatch thread. It is thus single threaded, no
	 * race condition expected.
	 */
	class TaskTableModel extends AbstractTableModel {

		TaskTableModel() {
			super();
		}

		/** Indexed tasks. */
		private List<Task> monitoredTasks = new ArrayList<>();

		/** Unordered tasks &rarr; faster task lookup (may be overkill). */
		private Set<Task> tasksSet = new HashSet<>();

		/**
		 * Store the previous completion state of a certain task, before it was
		 * updated.
		 */
		private Map<Task,Double> previousCompletion = new HashMap<>();

		/** Start time -> stores when a task was added to this table model. */
		private Map<Task, Instant> startTime = new HashMap<>();

		int totalTasks = 0;
		double totalProgression = 0;

		void addOrUpdate(Task task) {
			if (!tasksSet.contains(task)) {
				monitoredTasks.add(task);
				tasksSet.add(task);
				int idx = monitoredTasks.size()-1;
				taskTableModel.fireTableRowsInserted(idx, idx+1);
				if (estimateTimeLeft) startTime.put(task,Instant.now());
				totalTasks++;
			} else {
				int idx = monitoredTasks.indexOf(task);
				taskTableModel.fireTableRowsUpdated(idx, idx+1);
				double previousProgression = previousCompletion.get(task);
				totalProgression-=previousProgression;
			}
			// Now updating global progression
			if (task.getProgressMaximum()==0) {
				previousCompletion.put(task,0.5);
				totalProgression+=0.5;
			} else {
				double currentProgression = (double)task.getProgressValue()/(double)task.getProgressMaximum();
				previousCompletion.put(task,currentProgression);
				totalProgression+=currentProgression;
			}
			globalProgression = totalProgression / totalTasks;
		}

		void removeTask(Task task) {
			if (tasksSet.contains(task)) {
				int indexRemoved = monitoredTasks.indexOf(task);
				monitoredTasks.remove(indexRemoved);
				tasksSet.remove(task);
				taskTableModel.fireTableRowsDeleted(indexRemoved, indexRemoved+1);
				double previousProgression = previousCompletion.get(task);
				totalProgression-=previousProgression;
				totalTasks--;
				previousCompletion.remove(task);
				if(totalTasks==0) {
					globalProgression = 0;
				} else {
					globalProgression = totalProgression / totalTasks;
				}
				if (estimateTimeLeft) startTime.remove(task);
			}
		}

		String getEstimateTimeMessage(Task task) {
			if (tasksSet.contains(task)) {
				if (task.getProgressMaximum() == 0) {
					return "? remaining";
				}
				Instant now = Instant.now();
				Duration elapsedTime = Duration.between(startTime.get(task), now);
				double completion = previousCompletion.get(task);
				if (completion==0) return "? remaining";
				double elapsedTimeInS = elapsedTime.getSeconds();
				double totalTimeInS = elapsedTime.getSeconds()*1.0/completion;
				double remainingTimeInS = totalTimeInS-elapsedTimeInS;
				Duration duration = Duration.ofSeconds((long)remainingTimeInS);
				return humanReadableFormat(duration)+" remaining";
			}
			return "";
		}

		@Override
		public synchronized int getRowCount() {
			return totalTasks;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0: return "Task";
				case 1: return "Stop";
			}
			return null;
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0: return Task.class;
				case 1: return Task.class;
			}
			return null;
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return false;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (rowIndex<monitoredTasks.size()) {
				Task task = monitoredTasks.get(rowIndex);
				switch (columnIndex) {
					case 0: return task;
					case 1: return task;
				}
			}
			return null;
		}

		public Task getTask(int rowIndex) {
			if (rowIndex<monitoredTasks.size()) {
				return monitoredTasks.get(rowIndex);
			}
			return null;
		}
	}

	/*
	 * From https://java-swing-tips.blogspot.com/2014/06/how-to-create-circular-progress.html
	 * UI for circular progress bar
	 */
	static class ProgressCircleUI extends BasicProgressBarUI {
		@Override public Dimension getPreferredSize(JComponent c) {
			Dimension d = super.getPreferredSize(c);
			int v = Math.max(d.width, d.height);
			d.setSize(v, v);
			return d;
		}
		@Override public void paint(Graphics g, JComponent c) {
			Insets b = progressBar.getInsets(); // area for border
			int barRectWidth  = progressBar.getWidth()  - b.right - b.left;
			int barRectHeight = progressBar.getHeight() - b.top - b.bottom;
			if (barRectWidth <= 0 || barRectHeight <= 0) {
				return;
			}

			// draw the cells
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setPaint(progressBar.getForeground());
			double degree = 360 * progressBar.getPercentComplete();
			double sz = Math.min(barRectWidth, barRectHeight);
			double cx = b.left + barRectWidth  * .5;
			double cy = b.top  + barRectHeight * .5;
			double or = sz * .5;
			double ir = or * .5; //or - 20;
			Shape inner = new Ellipse2D.Double(cx - ir, cy - ir, ir * 2, ir * 2);
			Shape outer = new Arc2D.Double(
				cx - or, cy - or, sz, sz, 90 - degree, degree, Arc2D.PIE);
			Area area = new Area(outer);
			area.subtract(new Area(inner));
			g2.fill(area);
			g2.dispose();

			// Deal with possible text painting
			if (progressBar.isStringPainted()) {
				paintString(g, b.left, b.top, barRectWidth, barRectHeight, 0, b);
			}
		}
	}

	/**
	 * Class that serves as a Task renderer for a JTable
	 */
	class TaskRenderer implements TableCellRenderer {
		boolean isBordered;
		JPanel cell = new JPanel(); // container for top label, progressbar and bottom label
		JLabel labelTop = new JLabel(); // top label : task name and status
		JProgressBar progressBar = new JProgressBar(); // standard linear progress bar
		JLabel labelBottom = new JLabel(); // bottom label : task completion, and optionally time left
		Icon errorIcon = UIManager.getIcon("OptionPane.errorIcon"); // icon for canceling task
		JLabel cancelTask; // container for errorIcon

		public TaskRenderer(boolean isBordered) {
			labelTop.setVerticalTextPosition(SwingConstants.TOP);
			labelTop.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
			labelBottom.setVerticalTextPosition(SwingConstants.TOP);
			labelBottom.setBorder(new EmptyBorder(new Insets(0,0,0,0)));
			this.isBordered = isBordered;
			cell.setOpaque(true);
			cell.setLayout(new MigLayout());
			cell.add(labelTop,"height ::14, span");
			cell.add(progressBar,"height ::3, span");
			cell.add(labelBottom, "height ::14");
			cancelTask = new JLabel(errorIcon, SwingConstants.CENTER);
			cancelTask.setOpaque(true);
			cancelTask.setBackground(cell.getBackground());
		}

		@Override
		public Component getTableCellRendererComponent(
			JTable table, Object tk,
			boolean isSelected, boolean hasFocus,
			int row, int column) {
			if (column==1) {
				return cancelTask; // second column : stop icon
			}
			// first column : task information
			Task task = (Task) tk;
			String status = task.getStatusMessage();
			labelTop.setText(task.getName() + " " + ((status != null) ? status : ""));
			if (estimateTimeLeft) {
				labelBottom.setText(format(task.getProgressValue()) + "/" + format(task.getProgressMaximum()) + " - " + taskTableModel.getEstimateTimeMessage(task));
			} else {
				labelBottom.setText(format(task.getProgressValue()) + "/" + format(task.getProgressMaximum()));
			}
			if (task.getProgressMaximum() == 0) {
				progressBar.setMaximum(100);
				progressBar.setValue(50);
			} else {
				int progress = (int) ((100 * task.getProgressValue()) / task.getProgressMaximum());
				progressBar.setMaximum(100);
				progressBar.setValue(progress);
			}
			cell.setToolTipText(task.getStatusMessage()); // tool tip text = current task status
			return cell;
		}

	}

	private static final NavigableMap<Long, String> suffixes = new TreeMap<>();
	static {
		suffixes.put(1_000L, "k");
		suffixes.put(1_000_000L, "M");
		suffixes.put(1_000_000_000L, "G");
		suffixes.put(1_000_000_000_000L, "T");
		suffixes.put(1_000_000_000_000_000L, "P");
		suffixes.put(1_000_000_000_000_000_000L, "E");
	}

	/* Stack overflow magic : https://stackoverflow.com/questions/4753251/how-to-go-about-formatting-1200-to-1-2k-in-java
	 * Format long values to k, m, M, G for order of tens
	 * 1200 -> "1k" 2 235 456 789 -> "2G"
	 * @param value
	 * @return a human readable format of a value
	 */
	static String format(long value) {
		//Long.MIN_VALUE == -Long.MIN_VALUE so we need an adjustment here
		if (value == Long.MIN_VALUE) return format(Long.MIN_VALUE + 1);
		if (value < 0) return "-" + format(-value);
		if (value < 1000) return Long.toString(value); //deal with easy case

		Map.Entry<Long, String> e = suffixes.floorEntry(value);
		Long divideBy = e.getKey();
		String suffix = e.getValue();

		long truncated = value / (divideBy / 10); //the number part of the output times 10
		boolean hasDecimal = truncated < 100 && (truncated / 10d) != (truncated / 10);
		return hasDecimal ? (truncated / 10d) + suffix : (truncated / 10) + suffix;
	}

	// Thank you stack overflow https://stackoverflow.com/questions/3471397/how-can-i-pretty-print-a-duration-in-java
	static String humanReadableFormat(Duration duration) {
		return duration.toString()
				.substring(2)
				.replaceAll("(\\d[HMS])(?!$)", "$1 ")
				.toLowerCase();
	}
}
