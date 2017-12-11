package org.scijava.ui.swing.widget;

// TEMP - Move this to org.scijava.util
public class IntRange {
	private int min;
	private int max;
	public IntRange(final int min, final int max) {
		this.min = min;
		this.max = max;
	}
	public int min() { return min; }
	public int max() { return max; }
}
