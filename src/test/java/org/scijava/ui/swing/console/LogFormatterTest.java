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
