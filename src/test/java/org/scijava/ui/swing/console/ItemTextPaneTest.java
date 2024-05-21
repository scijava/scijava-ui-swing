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

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

import org.junit.Test;

/**
 * Tests {@link ItemTextPane}
 *
 * @author Matthias Arzt
 */
public class ItemTextPaneTest {

	private AttributeSet style = new SimpleAttributeSet();

	private List<ItemTextPane.Item> list = Arrays.asList(
			new ItemTextPane.Item(style, "XYZ\n"),
			new ItemTextPane.Item(style, "Foo "),
			new ItemTextPane.Item(style, "Bar"),
			new ItemTextPane.Item(style, "\n"),
			new ItemTextPane.Item(style, "Hello ")
			);

	@Test
	public void testCombiningItems() throws BadLocationException {
		ItemTextPane.DocumentCalculator calculator = new ItemTextPane.DocumentCalculator(list.iterator());
		calculator.update();
		Document doc = calculator.document();
		assertEquals("XYZ\nFoo Bar\nHello ", doc.getText(0, doc.getLength()));
	}
}
