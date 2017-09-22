/*
 * #%L
 * SciJava Common shared library for SciJava software.
 * %%
 * Copyright (C) 2009 - 2017 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, Max Planck
 * Institute of Molecular Cell Biology and Genetics, University of
 * Konstanz, and KNIME GmbH.
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.List;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import net.miginfocom.swing.MigLayout;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileListWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of {@link FileListWidget}.
 * 
 * @author Jan Eglinger
 */
@Plugin(type = InputWidget.class)
public class SwingFileListWidget extends SwingInputWidget<File[]> implements
		FileListWidget<JPanel>, ActionListener, MouseListener {

	@Parameter
	private UIService uiService;

	@Parameter
	private LogService logService;

	private JList<File> paths;
	private JButton addFilesButton;
	private JButton removeFilesButton;
	private JButton clearButton;

	@Override
	public File[] getValue() {
		DefaultListModel<File> listModel = //
			(DefaultListModel<File>) paths.getModel();
		List<File> fileList = Collections.list(listModel.elements());
		return fileList.toArray(new File[fileList.size()]);
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		paths = new JList<>(new DefaultListModel<>());
		//paths.setMinimumSize(new Dimension(150, 50));
		//paths.setMaximumSize(new Dimension(150, 50));
		paths.setDragEnabled(true);
		final String style = model.getItem().getWidgetStyle();
		paths.setTransferHandler(new FileListTransferHandler(style));
		JScrollPane scrollPane = new JScrollPane(paths);
		scrollPane.setPreferredSize(new Dimension(350, 100));
		paths.addMouseListener(this);
		// scrollPane.addMouseListener(l);

		setToolTip(scrollPane);
		getComponent().add(scrollPane);

		getComponent().add(Box.createHorizontalStrut(3));

		// GroupLayout buttonLayout = new GroupLayout(getComponent());
		JPanel buttonPanel = new JPanel(new MigLayout());

		addFilesButton = new JButton("Add files...");
		setToolTip(addFilesButton);
		buttonPanel.add(addFilesButton, "wrap, grow");
		addFilesButton.addActionListener(this);

		removeFilesButton = new JButton("Remove selected");
		setToolTip(removeFilesButton);
		buttonPanel.add(removeFilesButton, "wrap, grow");
		removeFilesButton.addActionListener(this);

		clearButton = new JButton("Clear list");
		setToolTip(clearButton);
		buttonPanel.add(clearButton, "grow");
		clearButton.addActionListener(this);
		
		getComponent().add(buttonPanel);

		/*
		getComponent().setLayout(buttonLayout);
		buttonLayout.setVerticalGroup(buttonLayout.createSequentialGroup()
				.addComponent(addFilesButton)
				.addComponent(removeFilesButton)
				.addComponent(clearButton));
		*/

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(File[].class);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		DefaultListModel<File> listModel = //
			(DefaultListModel<File>) paths.getModel();
		List<File> fileList = Collections.list(listModel.elements());

		if (e.getSource() == addFilesButton) {
			// Add new files
			// parse style attribute to allow choosing
			// files and/or directories, and filter files
			final WidgetModel widgetModel = get();
			final String widgetStyle = widgetModel.getItem().getWidgetStyle();
			FileFilter filter = SwingFileWidget.createFileFilter(widgetStyle);

			String style;
			if (widgetModel.isStyle(FileListWidget.FILES_AND_DIRECTORIES)) {
				style = FileListWidget.FILES_AND_DIRECTORIES;
			} else if (widgetModel.isStyle(FileListWidget.DIRECTORIES_ONLY)) {
				style = FileListWidget.DIRECTORIES_ONLY;
			} else {
				style = FileListWidget.FILES_ONLY; // default
			}

			fileList = uiService.chooseFiles(null, fileList, filter, style);
			if (fileList == null)
				return;
			for (File file : fileList) {
				listModel.addElement(file);
			}
		} else if (e.getSource() == removeFilesButton) {
			// Remove selected files
			List<File> selected = paths.getSelectedValuesList();
			for (File f : selected) {
				listModel.removeElement(f);
			}
		} else if (e.getSource() == clearButton) {
			// Clear the file selection
			listModel.removeAllElements();
		}
		paths.setModel(listModel);
		updateModel();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	protected void doRefresh() {
		File[] files = (File[]) get().getValue();
		DefaultListModel<File> listModel = new DefaultListModel<>();
		if (files != null) {
			for (File file : files) {
				listModel.addElement(file);
			}
		}
		paths.setModel(listModel);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// handle double click
		if (e.getClickCount() == 2) {
			DefaultListModel<File> listModel = //
				(DefaultListModel<File>) paths.getModel();
			// Remove selected files
			List<File> selected = paths.getSelectedValuesList();
			for (File f : selected) {
				listModel.removeElement(f);
			}
			paths.setModel(listModel);
			updateModel();
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// Nothing to do
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Nothing to do
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// Nothing to do
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// Nothing to do
	}

	// -- Helper classes --

	private class FileListTransferHandler extends TransferHandler {

		private final String style;

		public FileListTransferHandler(final String style) {
			this.style = style;
		}

		@Override
		public boolean canImport(final TransferHandler.TransferSupport support) {
			return SwingFileWidget.hasFiles(support);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(final TransferHandler.TransferSupport support) {
			final List<File> allFiles = SwingFileWidget.getFiles(support);
			if (allFiles == null) return false;
			final FileFilter filter = SwingFileWidget.createFileFilter(style);
			final List<File> files = SwingFileWidget.filterFiles(allFiles, filter);
			if (allFiles.size() != files.size()) {
				logService.warn("Some files were excluded " +
					"for not matching the input requirements (" + style + ")");
			}
			final JList<File> jlist = (JList<File>) support.getComponent();
			final DefaultListModel<File> model = (DefaultListModel<File>) //
				jlist.getModel();
			files.forEach(f -> model.addElement(f));
			jlist.setModel(model);
			updateModel();
			return true;
		}
	}
}
