package org.scijava.ui.swing.plot

import org.jetbrains.letsPlot.geom.geomDensity2D
import org.jetbrains.letsPlot.geom.geomPoint
import org.jetbrains.letsPlot.intern.toSpec
import org.jetbrains.letsPlot.letsPlot
import org.jetbrains.letsPlot.scale.scaleFillGradient
import org.jetbrains.letsPlot.themes.theme
import org.jetbrains.letsPlot.awt.plot.PlotPanel
import javax.swing.JFrame
import javax.swing.WindowConstants
import java.awt.Dimension
import java.util.Random

fun main() {
    // Generate sample data - two clusters
    val n = 1000
    val random = Random(42)
    val data = buildMap {
        val x = mutableListOf<Double>()
        val y = mutableListOf<Double>()

        repeat(n) {
            if (random.nextDouble() < 0.6) {
                // First cluster
                x.add(random.nextGaussian() * 0.5 + 2)
                y.add(random.nextGaussian() * 0.5 + 2)
            } else {
                // Second cluster
                x.add(random.nextGaussian() * 0.3 + 4)
                y.add(random.nextGaussian() * 0.3 + 4)
            }
        }
        put("x", x)
        put("y", y)
    }

    // Create the plot
    val plot = letsPlot(data) +
            geomDensity2D(
                alpha = 0.5,
                bins = 10,
                colorBy = "density",
                mapping = {
                    x = "x"
                    y = "y"
                }
            ) +
            geomDensity2D(
                color = "black",
                size = 0.5,
                bins = 10,
                mapping = {
                    x = "x"
                    y = "y"
                }
            ) +
            geomPoint(
                alpha = 0.3,
                size = 1.0,
                mapping = {
                    x = "x"
                    y = "y"
                }
            ) +
            scaleFillGradient(low = "#E0F7FA", high = "#01579B") +
            theme()

    // Create Swing window
    val panel = PlotPanel(plot)
    panel.preferredSize = Dimension(800, 600)

    val frame = JFrame("Bivariate KDE")
    frame.contentPane.add(panel)
    frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
    frame.pack()
    frame.isVisible = true
}
