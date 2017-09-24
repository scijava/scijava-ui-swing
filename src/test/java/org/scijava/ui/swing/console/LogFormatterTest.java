package org.scijava.ui.swing.console;

import org.junit.Test;
import org.scijava.Context;
import org.scijava.prefs.PrefService;

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link LogFormatter}.
 *
 * @author Matthias Arzt
 */
public class LogFormatterTest {

	@Test
	public void testPrefService() {
		PrefService prefService = new Context(PrefService.class).service(PrefService.class);
		String expected = "Hello World";
		prefService.put("foo", expected);
		String actual = prefService.get("foo");
		assertEquals(expected, actual);
	}

	@Test
	public void testPrefServiceMap() {
		PrefService prefService = new Context(PrefService.class).service(PrefService.class);
		Map<String, String> expected = Collections.singletonMap("Hello", "World");
		prefService.putMap("/foo", expected);
		Map<String, String> actual = prefService.getMap("/foo");
		assertEquals(expected, actual);
	}

	@Test
	public void testSettings() {
		PrefService prefService = new Context(PrefService.class).service(PrefService.class);
		LogFormatter formatter1 = new LogFormatter();
		formatter1.setPrefService(prefService, "/abc");
		formatter1.setVisible(LogFormatter.Field.ATTACHMENT, true);
		formatter1.setVisible(LogFormatter.Field.LEVEL, false);
		LogFormatter formatter2 = new LogFormatter();
		formatter2.setPrefService(prefService, "/abc");
		assertTrue(formatter2.isVisible(LogFormatter.Field.ATTACHMENT));
		assertFalse(formatter2.isVisible(LogFormatter.Field.LEVEL));
	}
}
