/*
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2021 SciJava developers.
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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.UIService;
import org.scijava.widget.FileListWidget;
import org.scijava.widget.FileWidget;
import org.scijava.widget.InputWidget;
import org.scijava.widget.WidgetModel;

/**
 * Swing implementation of file selector widget.
 * 
 * @author Curtis Rueden
 */
@Plugin(type = InputWidget.class)
public class SwingFileWidget extends SwingInputWidget<File> implements
	FileWidget<JPanel>, ActionListener, DocumentListener
{

	@Parameter
	private UIService uiService;

	private JTextField path;
	private JButton browse;

	// -- InputWidget methods --

	@Override
	public File getValue() {
		final String text = path.getText();
		return text.isEmpty() ? null : new File(text);
	}

	// -- WrapperPlugin methods --

	@Override
	public void set(final WidgetModel model) {
		super.set(model);

		path = new JTextField(16);
		path.setDragEnabled(true);
		final String style = model.getItem().getWidgetStyle();
		path.setTransferHandler(new FileTransferHandler(style));
		setToolTip(path);
		getComponent().add(path);
		path.getDocument().addDocumentListener(this);

		getComponent().add(Box.createHorizontalStrut(3));

		browse = new JButton("Browse");
		setToolTip(browse);
		getComponent().add(browse);
		browse.addActionListener(this);

		refreshWidget();
	}

	// -- Typed methods --

	@Override
	public boolean supports(final WidgetModel model) {
		return super.supports(model) && model.isType(File.class);
	}

	// -- ActionListener methods --

	@Override
	public void actionPerformed(final ActionEvent e) {
		File file = new File(path.getText());
		if (!file.isDirectory()) {
			file = file.getParentFile();
		}

		// display file chooser in appropriate mode
		final WidgetModel model = get();
		final String style;
		if (model.isStyle(FileWidget.DIRECTORY_STYLE)) {
			style = FileWidget.DIRECTORY_STYLE;
		}
		else if (model.isStyle(FileWidget.SAVE_STYLE)) {
			style = FileWidget.SAVE_STYLE;
		}
		else {
			style = FileWidget.OPEN_STYLE;
		}
		file = uiService.chooseFile(file, style);
		if (file == null) return;

		path.setText(file.getAbsolutePath());
	}

	// -- DocumentListener methods --

	@Override
	public void changedUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void insertUpdate(final DocumentEvent e) {
		updateModel();
	}

	@Override
	public void removeUpdate(final DocumentEvent e) {
		updateModel();
	}

	// -- AbstractUIInputWidget methods ---

	@Override
	public void doRefresh() {
		final String text = get().getText();
		if (text.equals(path.getText())) return; // no change
		path.setText(text);
	}

	// -- Utility methods --

	/**
	 * Creates a {@link FileFilter} that filters files and/or directories
	 * according to the given widget style.
	 * <p>
	 * It supports filtering files by extension as specified by the syntax
	 * {@code extensions:ext1/ext2} where {@code ext1}, {@code ext2}, etc., are
	 * extensions to accept. It also filters files and/or directories as specified
	 * by the following styles:
	 * </p>
	 * <ul>
	 * <li>{@link FileWidget#OPEN_STYLE}</li>
	 * <li>{@link FileWidget#SAVE_STYLE}</li>
	 * <li>{@link FileListWidget#FILES_ONLY}</li>
	 * <li>{@link FileWidget#DIRECTORY_STYLE}</li>
	 * <li>{@link FileListWidget#DIRECTORIES_ONLY}</li>
	 * <li>{@link FileListWidget#FILES_AND_DIRECTORIES}</li>
	 * </ul>
	 * 
	 * @param widgetStyle The style defining which files get accepted by the
	 *          filter.
	 * @return A {@link FileFilter} that accepts files matching the given widget
	 *         style.
	 */
	public static FileFilter createFileFilter(final String widgetStyle) {
		final List<String> filesOnlyStyles = Arrays.asList(
			FileWidget.OPEN_STYLE, FileWidget.SAVE_STYLE, FileListWidget.FILES_ONLY
		);
		final List<String> dirsOnlyStyles = Arrays.asList(
			FileWidget.DIRECTORY_STYLE, FileListWidget.DIRECTORIES_ONLY
		);
		final List<String> filesAndDirsStyles = Arrays.asList(
			FileListWidget.FILES_AND_DIRECTORIES
		);

		final List<String> exts = new ArrayList<>();
		boolean filesOnly = false, dirsOnly = false, filesAndDirs = false;
		if (widgetStyle != null) {
			// Extract extensions to be accepted.
			for (final String token : widgetStyle.split(",")) {
				if (filesOnlyStyles.contains(token)) filesOnly = true;
				if (dirsOnlyStyles.contains(token)) dirsOnly = true;
				if (filesAndDirsStyles.contains(token)) filesAndDirs = true;
				if (token.startsWith("extensions")) {
					String extensions = token.split(":")[1];
					for (final String ext : extensions.split("/"))
						exts.add(ext);
				}
			}
		}
		// NB: If none of the styles was set, we do the default behavior.
		final boolean defaultBehavior = !(filesOnly || dirsOnly || filesAndDirs);

		final boolean rejectFiles = dirsOnly;
		// NB: We reject directories by default, if no styles are given.
		final boolean rejectDirs = filesOnly || defaultBehavior;
		return new FileFilter() {
			@Override
			public boolean accept(final File pathname) {
				if (pathname.isFile() && rejectFiles) return false;
				if (pathname.isDirectory() && rejectDirs) return false;
				if (exts.isEmpty()) return true;
				if (pathname.isDirectory()) return true; // Don't filter dirs by ext.
				for (final String ext : exts) {
					if (pathname.getName().endsWith("." + ext)) return true;
				}
				return false;
			}
		};
	}

	/**
	 * Checks whether the given drag and drop operation offers a list of files as
	 * one of its flavors.
	 * 
	 * @param support The drag and drop operation that should be checked.
	 * @return True iff the operation can provide a list of files.
	 */
	public static boolean hasFiles(
		final TransferHandler.TransferSupport support)
	{
		// Check the flavors to make sure one of them is file list.
		for (final DataFlavor flavor : support.getDataFlavors()) {
			if (flavor.isFlavorJavaFileListType()) return true;
		}
		return false;
	}

	/**
	 * Gets the list of files associated with the given drag and drop operation.
	 * 
	 * @param support The drag and drop operation from which files should be
	 *          extracted.
	 * @return The list of files, or null if something goes wrong: operation does
	 *         not provide a list; the list contains something other than
	 *         {@link File} objects; or an exception is thrown.
	 */
	public static List<File> getFiles(
		final TransferHandler.TransferSupport support)
	{
		try {
			final Object files = support.getTransferable().getTransferData(
				DataFlavor.javaFileListFlavor);

			// NB: Be absolutely sure the files object is a List<File>!
			if (!(files instanceof List)) return null;
			@SuppressWarnings("rawtypes")
			final List list = (List) files;
			for (int i=0; i<list.size(); i++) {
				if (!(list.get(i) instanceof File)) return null;
			}
			@SuppressWarnings({ "unchecked", "cast" })
			List<File> listOfFiles = (List<File>) list;
			return listOfFiles;
		}
		catch (final UnsupportedFlavorException | IOException exc) {
			return null;
		}
	}

	/**
	 * Gets the String content of the current transfer support
	 * 
	 * @param support
	 *            The paste (or drag and drop) operation from which text should
	 *            be extracted.
	 * 
	 * @return The pasted (or dropped) text
	 */
	public static String getText(final TransferHandler.TransferSupport support) {
		try {
			return (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
		} catch (UnsupportedFlavorException | IOException exc) {
			return "";
		}

	}

	/**
	 * Filters the given list of files according to the specified
	 * {@link FileFilter}.
	 * 
	 * @param list The list of files to filter.
	 * @param filter The filter to use.
	 * @return A newly created list including only the matching files.
	 */
	public static List<File> filterFiles(final List<File> list,
		final FileFilter filter)
	{
		return list.stream().filter(filter::accept).collect(Collectors.toList());
	}

	// -- Helper classes --

	private class FileTransferHandler extends TransferHandler {

		private final String style;

		public FileTransferHandler(final String style) {
			this.style = style;
		}

		@Override
		public boolean canImport(final TransferHandler.TransferSupport support) {
			if (!hasFiles(support)) return false;

			// We wish to test the content of the transfer data and
			// determine if they are (a) files and (b) files we are
			// actually interested in processing. So we need to call
			// getTransferData() so that we can inspect the file names.
			// Unfortunately, this will not always work.
			//    Under Windows, the Transferable instance
			// will have transfer data ONLY while the mouse button is
			// depressed. However, when the user releases the mouse
			// button, this method will be called one last time. And when
			// when this method attempts to getTransferData, Java will throw
			// an InvalidDnDOperationException. Since we know that the
			// exception is coming, we simply catch it and ignore it.
			// See:
			// https://coderanch.com/t/664525/java/Invalid-Drag-Drop-Exception
			try {
				final List<File> allFiles = getFiles(support);
				if (allFiles == null || allFiles.size() != 1)
					return false;

				final FileFilter filter = SwingFileWidget
						.createFileFilter(style);
				final List<File> files = SwingFileWidget.filterFiles(allFiles,
						filter);
				return files.size() == 1;
			} catch (InvalidDnDOperationException exc) {
				return true;
			}
		}

		@Override
		public boolean importData(TransferHandler.TransferSupport support) {
			final List<File> files = getFiles(support);
			if (files == null) {
				String text = getText(support);
				if (text.equals(""))
					return false;
				// TODO check if text matches filter/style
				((JTextField) support.getComponent()).setText(text);
				return true;
			}
			if (files.size() != 1) return false;

			final File file = files.get(0);
			((JTextField) support.getComponent()).setText(file.getAbsolutePath());
			return true;
		}
	}
}
