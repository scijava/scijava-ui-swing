/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2026 SciJava developers.
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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TooManyListenersException;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;

import net.miginfocom.swing.MigLayout;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.widget.FileListWidget;
import org.scijava.widget.FileWidget;
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
	private LogService logService;

	private boolean loadingFolder;

	private JList<File> paths;
	private JButton addFilesButton;
	private JButton addFolderButton;
	private JButton removeFilesButton;
	private JButton clearButton;

	@Override
	public File[] getValue() {
		DefaultListModel<File> listModel = //
			(DefaultListModel<File>) paths.getModel();
		List<File> fileList = Collections.list(listModel.elements());
		return fileList.toArray(new File[0]);
	}

	@Override
	public void set(final WidgetModel model) {
		super.set(model);
		paths = new JList<>(new DefaultListModel<>());
		paths.setDragEnabled(true);
		final String style = model.getItem().getWidgetStyle();
		paths.setTransferHandler(new FileListTransferHandler(style, paths
			.getTransferHandler()));
		JScrollPane scrollPane = new JScrollPane(paths);
		scrollPane.setPreferredSize(new Dimension(350, 100));
		paths.addMouseListener(this);
		// scrollPane.addMouseListener(l);

		setToolTip(scrollPane);
		getComponent().add(scrollPane, "grow");

		getComponent().add(Box.createHorizontalStrut(3));

		// no insets and a 3 px vertical gap, to match the other file widgets
		JPanel buttonPanel = new JPanel(new MigLayout("ins 0, gapy 3"));

		// Set button label dependent on widget style
		String filesLabel = model.isStyle(DIRECTORIES_ONLY) ? "Add Folders..."
				: "Add Files...";
		addFilesButton = new JButton(filesLabel);
		setToolTip(addFilesButton);
		buttonPanel.add(addFilesButton, "wrap, grow");
		addFilesButton.addActionListener(this);

		// Only show folder button if style allows files
		if (!model.isStyle(DIRECTORIES_ONLY)) {
			addFolderButton = new JButton("Add Folder...");
			// set directly: setToolTip() would replace it with the description
			addFolderButton.setToolTipText("<html>Adds all matching files from the " +
				"chosen folder<br>and its subfolders (recursive)</html>");
			// add TransferHandler to accept dropped folders
			addFolderButton.setTransferHandler(new FolderTransferHandler());

			DropTarget dropTarget = addFolderButton.getDropTarget();
			try {
				dropTarget.addDropTargetListener(new ButtonDropTargetListener());
			} catch (TooManyListenersException exc) {
				logService.error("Error with setting up drop support", exc);
			}

			buttonPanel.add(addFolderButton, "wrap, grow");
			addFolderButton.addActionListener(this);
		}

		removeFilesButton = new JButton("Remove");
		// set directly: setToolTip() would replace it with the description
		removeFilesButton.setToolTipText("<html>Removes the selected entries " +
			"from the list<br>(Delete or double-clicking an entry also removes it)</html>");
		removeFilesButton.setEnabled(false);
		paths.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
				removeFilesButton.setEnabled(!paths.isSelectionEmpty());
		});
		// Delete (or Backspace, e.g. the macOS delete key) removes the selection
		final String removeKey = "removeSelected";
		for (final int key : new int[] { KeyEvent.VK_DELETE,
			KeyEvent.VK_BACK_SPACE })
		{
			paths.getInputMap().put(KeyStroke.getKeyStroke(key, 0), removeKey);
		}
		paths.getActionMap().put(removeKey, new AbstractAction() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				removeSelected();
			}
		});
		buttonPanel.add(removeFilesButton, "wrap, grow");
		removeFilesButton.addActionListener(this);

		clearButton = new JButton("Clear");
		setToolTip(clearButton);
		buttonPanel.add(clearButton, "grow");
		clearButton.addActionListener(this);
		
		getComponent().add(buttonPanel);

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

		if (e.getSource() == addFilesButton) {
			// Add new files
			// parse style attribute to allow choosing
			// files and/or directories, and filter files
			List<File> fileList = Collections.list(listModel.elements());
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

			fileList = chooseFiles(null, fileList, filter, style);
			if (fileList == null)
				return;
			addUnique(fileList);
			updateModel();
			return;
		} else if (e.getSource() == addFolderButton) {
			File folder = chooseFile(null, FileWidget.DIRECTORY_STYLE);
			if (folder == null)
				return;
			addFolderContent(folder);
			return;
		} else if (e.getSource() == removeFilesButton) {
			removeSelected();
			return;
		} else if (e.getSource() == clearButton) {
			// Clear the file selection
			listModel.removeAllElements();
		}
		paths.setModel(listModel);
		updateButtons();
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
		updateButtons();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// handle double click
		if (e.getClickCount() == 2) {
			removeSelected();
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

	// -- Helper methods --

	/** Removes the selected entries from the list */
	private void removeSelected() {
		final DefaultListModel<File> listModel = //
			(DefaultListModel<File>) paths.getModel();
		for (final File f : paths.getSelectedValuesList()) {
			listModel.removeElement(f);
		}
		paths.setModel(listModel);
		updateButtons();
		updateModel();
	}

	/** Adds the given files to the list, skipping those already listed */
	private void addUnique(final List<File> files) {
		final DefaultListModel<File> current = //
			(DefaultListModel<File>) paths.getModel();
		// Replace the model at once, instead of firing an event per file
		final DefaultListModel<File> merged = new DefaultListModel<>();
		final Set<File> seen = new HashSet<>();
		Collections.list(current.elements()).forEach(f -> {
			merged.addElement(f);
			seen.add(f);
		});
		files.stream().filter(seen::add).forEach(merged::addElement);
		paths.setModel(merged);
		updateButtons();
	}

	/** Enables the buttons that only make sense for a non-empty list */
	private void updateButtons() {
		clearButton.setEnabled(paths.getModel().getSize() > 0);
	}

	/**
	 * Adds the files of a folder (and its subfolders) that match the widget
	 * style. The folder is scanned in the background, so the UI stays responsive
	 */
	private void addFolderContent(final File folder) {
		if (loadingFolder) return;
		loadingFolder = true;
		final FileFilter filter = SwingFileWidget.createFileFilter(get().getItem()
			.getWidgetStyle());
		getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		if (addFolderButton != null) addFolderButton.setEnabled(false);
		new SwingWorker<List<File>, Void>() {

			@Override
			protected List<File> doInBackground() {
				return getFilesFromFolder(folder, filter);
			}

			@Override
			protected void done() {
				try {
					addUnique(get());
					updateModel();
				}
				catch (final InterruptedException | ExecutionException exc) {
					logService.error("Error when trying to retrieve file list", exc);
				}
				finally {
					loadingFolder = false;
					getComponent().setCursor(Cursor.getDefaultCursor());
					if (addFolderButton != null) addFolderButton.setEnabled(true);
				}
			}
		}.execute();
	}

	/** Walks the folder tree. Must not be called on the EDT */
	private List<File> getFilesFromFolder(final File inputFolder,
		final FileFilter filter)
	{
		try (final Stream<Path> walk = Files.walk(
			inputFolder.toPath()))
		{
			return walk.map(Path::toFile).filter(filter::accept)
				.collect(Collectors.toList());
		}
		catch (IOException | UncheckedIOException exc) {
			logService.error("Error when trying to retrieve file list", exc);
			return new ArrayList<>();
		}
	}

	// -- Helper classes --

	private class FileListTransferHandler extends TransferHandler {

		private final String style;
		private final TransferHandler fallback;

		public FileListTransferHandler(final String style,
			final TransferHandler fallback)
		{
			this.style = style;
			this.fallback = fallback;
		}

		// Keep copy and drag out of the list working (default handler)
		@Override
		public int getSourceActions(final JComponent c) {
			return fallback == null ? NONE : fallback.getSourceActions(c);
		}

		@Override
		public void exportToClipboard(final JComponent comp,
			final Clipboard clip, final int action) throws IllegalStateException
		{
			if (fallback != null) fallback.exportToClipboard(comp, clip, action);
		}

		@Override
		public void exportAsDrag(final JComponent comp, final InputEvent e,
			final int action)
		{
			if (fallback != null) fallback.exportAsDrag(comp, e, action);
		}

		@Override
		public boolean canImport(final TransferHandler.TransferSupport support) {
			return SwingFileWidget.hasFiles(support);
		}

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
			addUnique(files);
			updateModel();
			return true;
		}
	}

	private class FolderTransferHandler extends TransferHandler {
		@Override
		public boolean canImport(final TransferHandler.TransferSupport support) {
			return SwingFileWidget.hasFiles(support);
		}

		@Override
		public boolean importData(final TransferHandler.TransferSupport support) {
			// getFiles from support
			final List<File> files = SwingFileWidget.getFiles(support);
			if (files == null || files.size() != 1)
				return false;

			// check if it's a folder
			final File folder = files.get(0);
			if (!folder.isDirectory())
				return false;

			// add all files matching filter from folder and subfolders
			addFolderContent(folder);
			return true;
		}
	}
	
	private static class ButtonDropTargetListener extends DropTargetAdapter {

		@Override
		public void drop(DropTargetDropEvent dtde) {
			JButton button = (JButton) dtde.getDropTargetContext()
					.getComponent();
			button.getModel().setPressed(false);
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
		}

		@Override
		public void dragEnter(DropTargetDragEvent dtde) {
			JButton button = (JButton) dtde.getDropTargetContext()
					.getComponent();
			button.getModel().setPressed(true);
		}

		@Override
		public void dragExit(DropTargetEvent dte) {
			JButton button = (JButton) dte.getDropTargetContext()
					.getComponent();
			button.getModel().setPressed(false);
		}
	}
}
