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

package org.scijava.ui.swing.viewer.plot.jfreechart;

import org.scijava.plot.MarkerStyle;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Matthias Arzt
 */

class AwtMarkerStyles {

	private static final double DEFAULT_SIZE = 3.0;

	private final boolean visible;

	private final boolean filled;

	private final Shape shape;

	private AwtMarkerStyles(boolean visible, boolean filled, Shape shape) {
		this.visible = visible;
		this.filled = filled;
		this.shape = shape;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isFilled() {
		return filled;
	}

	public Shape getShape() {
		return shape;
	}

	public static AwtMarkerStyles getInstance(MarkerStyle style) {
		return getInstance(style, DEFAULT_SIZE);
	}

	public static AwtMarkerStyles getInstance(MarkerStyle style, double size) {
		if(style != null)
			switch (style) {
				case NONE:
					return none;
				case PLUS:
					return plus(size);
				case X:
					return x(size);
				case STAR:
					return star(size);
				case SQUARE:
					return square(size);
				case FILLEDSQUARE:
					return filledSquare(size);
				case CIRCLE:
					return circle(size);
				case FILLEDCIRCLE:
					return filledCircle(size);
				case DIAMOND:
					return diamond(size);
				case FILLEDDIAMOND:
					return filledDiamond(size);
				case TRIANGLE:
					return triangle(size);
				case FILLEDTRIANGLE:
					return filledTriangle(size);
			}
		return square(size);
	}

	// --- Helper Constants ---

	private static AwtMarkerStyles none = new AwtMarkerStyles(false, false, null);

	private static AwtMarkerStyles plus(double size) {return new AwtMarkerStyles(true, false, Shapes.plus(size));}

	private static AwtMarkerStyles x(double size) {return new AwtMarkerStyles(true, false, Shapes.x(size));}

	private static AwtMarkerStyles star(double size) {return new AwtMarkerStyles(true, false, Shapes.star(size));}

	private static AwtMarkerStyles square(double size) {return new AwtMarkerStyles(true, false, Shapes.square(size));}

	private static AwtMarkerStyles filledSquare(double size) {return new AwtMarkerStyles(true, true, Shapes.square(size));}

	private static AwtMarkerStyles circle(double size) {return new AwtMarkerStyles(true, false, Shapes.circle(size));}

	private static AwtMarkerStyles filledCircle(double size) {return new AwtMarkerStyles(true, true, Shapes.circle(size));}

	private static AwtMarkerStyles diamond(double size) {return new AwtMarkerStyles(true, false, Shapes.diamond(size));}

	private static AwtMarkerStyles filledDiamond(double size) {return new AwtMarkerStyles(true, true, Shapes.diamond(size));}

	private static AwtMarkerStyles triangle(double size) {return new AwtMarkerStyles(true, false, Shapes.triangle(size));}

	private static AwtMarkerStyles filledTriangle(double size) {return new AwtMarkerStyles(true, true, Shapes.triangle(size));}


	static private class Shapes {

		private static Shape x(double size) {return getAwtXShape(size);}

		private static Shape plus(double size) {return getAwtPlusShape(size);}

		private static Shape star(double size) {return getAwtStarShape(size);}

		private static Shape square(double size) {return getAwtSquareShape(size);}

		private static Shape circle(double size) {return getAwtCircleShape(size);}

		private static Shape diamond(double size) {return getAwtDiamondShape(size);}

		private static Shape triangle(double size) {return getAwtTriangleShape(size);}

		private static Shape getAwtXShape(double size) {
			final Path2D p = new Path2D.Double();
			final double s = size;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			return p;
		}

		private static Shape getAwtPlusShape(double size) {
			final Path2D p = new Path2D.Double();
			final double t = size + 1;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

		private static Shape getAwtStarShape(double size) {
			final Path2D p = new Path2D.Double();
			final double s = size;
			p.moveTo(-s, -s);
			p.lineTo(s, s);
			p.moveTo(s, -s);
			p.lineTo(-s, s);
			final double t = size + 1;
			p.moveTo(0, -t);
			p.lineTo(0, t);
			p.moveTo(t, 0);
			p.lineTo(-t, 0);
			return p;
		}

		private static Shape getAwtSquareShape(double size) {
			final double s = size;
			final double t = size * 2.0;
			return new Rectangle2D.Double(-s, -s, t, t);
		}

		private static Shape getAwtCircleShape(double size) {
			final double s = size;
			final double t = size * 2.0;
			return new Ellipse2D.Double(-s, -s, t, t);
		}

		private static Shape getAwtDiamondShape(double size) {
			final Path2D p = new Path2D.Double();
			final double s = size;
			p.moveTo(0, -s);
			p.lineTo(-s, 0);
			p.lineTo(0, s);
			p.lineTo(s, 0);
			p.lineTo(0, -s);
			return p;

		}

		private static Shape getAwtTriangleShape(double size) {
			final Path2D p = new Path2D.Double();
			final double s = size;
			p.moveTo(0, -s);
			p.lineTo(-s, s);
			p.lineTo(s, s);
			p.lineTo(0, -s);
			return p;
		}

	}

}
