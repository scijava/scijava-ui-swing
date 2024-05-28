package org.scijava.ui.swing.widget;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SwingFileWidgetTest
{

	@Test
	public void testFormatFileFilterExtensions() {
		final List< String > multipleExtensions = Arrays.asList( "jpg", "jpeg", "png", "gif", "bmp" );
		final List< String > singleExtension = Collections.singletonList( "xml" );
		assertEquals( "*.jpg;*.jpeg;*.png;*.gif;*.bmp", SwingFileWidget.formatFileFilterExtensions( multipleExtensions ) );
		assertEquals( "*.xml", SwingFileWidget.formatFileFilterExtensions( singleExtension ) );
	}
}
