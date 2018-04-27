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
		LogFormatter formatter1 = new LogFormatter(context, "abc");
		formatter1.setVisible(LogFormatter.Field.ATTACHMENT, true);
		formatter1.setVisible(LogFormatter.Field.LEVEL, false);
		LogFormatter formatter2 = new LogFormatter(context, "abc");
		assertTrue(formatter2.isVisible(LogFormatter.Field.ATTACHMENT));
		assertFalse(formatter2.isVisible(LogFormatter.Field.LEVEL));
	}
}
