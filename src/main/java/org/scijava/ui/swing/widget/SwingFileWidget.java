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

import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.formdev.flatlaf.FlatClientProperties;
import org.scijava.plugin.Plugin;
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
		path.putClientProperty(FlatClientProperties.TEXT_FIELD_SHOW_CLEAR_BUTTON, true);
		path.setDragEnabled(true);
		path.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, model
				.isStyle(FileWidget.DIRECTORY_STYLE)
				? "Browse, drop, or paste a folder path"
				: "Browse, drop, or paste a file path");
		final String style = model.getItem().getWidgetStyle();
		path.setTransferHandler(new FileTransferHandler(style, path.getTransferHandler()));
		setToolTip(path);
		getComponent().add(path);
		path.getDocument().addDocumentListener(this);

		getComponent().add(Box.createHorizontalStrut(3));

		browse = new JButton("Browse...");
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
		file = chooseFile(file, style);
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

			@Override
			public String toString() {
				return formatFileFilterExtensions( exts );
			}
		};
	}

	static String formatFileFilterExtensions( final List< String > extensions ) {
		List< String > extsWithPrefix = new ArrayList<>();
		for ( String extension : extensions ) {
			extsWithPrefix.add( "*." + extension );
		}
		StringJoiner joiner = new StringJoiner( ";" );
		extsWithPrefix.forEach( joiner::add );
		return joiner.toString();
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

	/** Briefly outlines the path field as invalid (FlatLaf only) */
	private void flashError() {
		path.putClientProperty(FlatClientProperties.OUTLINE, FlatClientProperties.OUTLINE_ERROR);
		final Timer timer = new Timer(1000, e -> path.putClientProperty(FlatClientProperties.OUTLINE, null));
		timer.setRepeats(false);
		timer.start();
	}

	/**
	 * Handles file drops, delegating everything else (text paste, copy, cut,
	 * text drag) to the default handler of the text field
	 */
	private class FileTransferHandler extends TransferHandler {

		private final String style;
		private final TransferHandler fallback;

		public FileTransferHandler(final String style, final TransferHandler fallback) {
			this.style = style;
			this.fallback = fallback;
		}

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
			if (!hasFiles(support)) {
				return fallback != null && fallback.canImport(support);
			}

			// Transfer data may not be readable while dragging (Windows, and
			// macOS). Accept and validate on drop
			try {
				final List<File> allFiles = getFiles(support);
				if (allFiles == null || allFiles.isEmpty()) return true;
				if (allFiles.size() != 1) return false;
				final FileFilter filter = SwingFileWidget.createFileFilter(style);
                return SwingFileWidget.filterFiles(allFiles, filter).size() == 1;
			}
			catch (final RuntimeException exc) {
				return true;
			}
		}

		@Override
		public boolean importData(final TransferHandler.TransferSupport support) {
			if (hasFiles(support)) {
				final List<File> files;
				try {
					files = getFiles(support);
				}
				catch (final RuntimeException exc) {
					flashError();
					return false;
				}
				if (files == null || files.size() != 1) {
					flashError();
					return false;
				}
				final FileFilter filter = SwingFileWidget.createFileFilter(style);
				if (SwingFileWidget.filterFiles(files, filter).isEmpty()) {
					flashError();
					return false;
				}
				path.setText(files.get(0).getAbsolutePath());
				return true;
			}
			return fallback != null && fallback.importData(support);
		}
	}
}
