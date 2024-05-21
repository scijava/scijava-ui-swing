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

package org.scijava.ui.swing.console;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.EnumSet;
import java.util.Map;

import org.scijava.Context;
import org.scijava.log.LogLevel;
import org.scijava.log.LogMessage;
import org.scijava.plugin.Parameter;
import org.scijava.prefs.PrefService;

/**
 * Used by {@link LoggingPanel} to simplify formatting log messages.
 *
 * @author Matthias Arzt
 */
public class LogFormatter {

	private final String prefKey;

	@Parameter(required = false)
	private PrefService prefService;

	public LogFormatter(final Context context, final String prefKey) {
		context.inject(this);
		this.prefKey = prefKey;
		applySettings();
	}

	public enum Field {
		TIME, LEVEL, SOURCE, MESSAGE, THROWABLE, ATTACHMENT
	}

	private EnumSet<Field> visibleFields = EnumSet.of(Field.TIME,
		Field.LEVEL, Field.SOURCE, Field.MESSAGE, Field.THROWABLE);

	public boolean isVisible(Field field) {
		return visibleFields.contains(field);
	}

	public void setVisible(Field field, boolean visible) {
		// copy on write to enable isVisible to be used concurrently
		EnumSet<Field> copy = EnumSet.copyOf(visibleFields);
		if (visible) copy.add(field);
		else copy.remove(field);
		visibleFields = copy;
		changeSetting(field, visible);
	}

	public String format(LogMessage message) {
		try {
			final StringWriter sw = new StringWriter();
			final PrintWriter printer = new PrintWriter(sw);

			if (isVisible(Field.TIME))
				printWithBrackets(printer, message.time().toString());

			if (isVisible(Field.LEVEL))
				printWithBrackets(printer, LogLevel.prefix(message.level()));

			if (isVisible(Field.SOURCE))
				printWithBrackets(printer, message.source().toString());

			if (isVisible(Field.ATTACHMENT)) {
				printer.print(message.attachments());
				printer.print(" ");
			}

			if (isVisible(Field.MESSAGE)) printer.println(message.text());

			if (isVisible(Field.THROWABLE) && message.throwable() != null)
				message.throwable().printStackTrace(printer);
			return sw.toString();
		}
		catch (Exception e) {
			return "[Exception while formatting log message: " + e + "]\n";
		}
	}

	private void printWithBrackets(PrintWriter printer, String prefix) {
		printer.append('[').append(prefix).append("] ");
	}

	// -- Helper methods --

	public void applySettings() {
		if (skipPersist()) return;
		final Map<String, String> settings = //
			prefService.getMap(LogFormatter.class, prefKey);
		for(Field field : Field.values()) {
			String defaultValue = Boolean.toString(isVisible(field));
			String value = settings.getOrDefault(field.toString(), defaultValue);
			setVisible(field, Boolean.valueOf(value));
		}
		visibleFields.toString();
	}

	public void changeSetting(Field field, boolean visible) {
		if (skipPersist()) return;
		Map<String, String> settings = prefService.getMap(LogFormatter.class, prefKey);
		settings.put(field.toString(), Boolean.toString(visible));
		prefService.put(LogFormatter.class, prefKey, settings);
	}

	private boolean skipPersist() {
		return prefService == null || prefKey == null || prefKey.isEmpty();
	}
}
