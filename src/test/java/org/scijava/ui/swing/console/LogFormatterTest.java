/*-
 * #%L
 * SciJava UI components for Java Swing.
 * %%
 * Copyright (C) 2010 - 2020 SciJava developers.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

/**
 * Tests {@link LogFormatter}.
 *
 * @author Matthias Arzt
 */
public class LogFormatterTest {

	@Test
	public void testSettings() {
		Context context = new Context(PrefService.class);
		final String usedKey = "LogFormatterTest-key";
		final String nonMatchingKey = "LogFormatterTest-nonmatching";
		context.service(PrefService.class).remove(LogFormatter.class, usedKey);
		context.service(PrefService.class).remove(LogFormatter.class, nonMatchingKey);

		// Assign some settings to a particular key.
		LogFormatter formatter1 = new LogFormatter(context, usedKey);
		formatter1.setVisible(LogFormatter.Field.ATTACHMENT, true);
		formatter1.setVisible(LogFormatter.Field.LEVEL, false);

		// Check that matching key does share these settings.
		LogFormatter formatter2 = new LogFormatter(context, usedKey);
		assertTrue(formatter2.isVisible(LogFormatter.Field.ATTACHMENT));
		assertFalse(formatter2.isVisible(LogFormatter.Field.LEVEL));

		// Check that non-matching key does not share these settings.
		LogFormatter formatter3 = new LogFormatter(context, nonMatchingKey);
		assertFalse(formatter3.isVisible(LogFormatter.Field.ATTACHMENT));
		assertTrue(formatter3.isVisible(LogFormatter.Field.LEVEL));
	}
}
