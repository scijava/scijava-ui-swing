package org.scijava.ui.swing.plot;

import org.jzy3d.analysis.AWTAbstractAnalysis;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.SurfaceBuilder;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Jzy3DDemo extends AWTAbstractAnalysis {
    private double[][] data;
    private int gridSize = 50;
    private double bandwidthX;
    private double bandwidthY;

    public Jzy3DDemo() {
        super();
        setFactory(new AWTChartFactory());
    }

    public static void main(String[] args) throws Exception {
        Jzy3DDemo demo = new Jzy3DDemo();
        demo.init();
        demo.getChart().open("Bivariate KDE", 800, 600);
    }

    @Override
    public Chart initializeChart() {
        Quality quality = Quality.Advanced();
        // Initialize the chart
        Chart chart = getFactory().newChart(quality);
        this.chart = chart;  // Store the chart in the parent class
        return chart;
    }

    @Override
    public void init() throws Exception {
        // First initialize the chart
        Chart chart = initializeChart();

        // Generate sample data - two clusters
        int n = 1000;
        Random rand = new Random(42);
        data = new double[n][2];

        // Generate two clusters
        for (int i = 0; i < n; i++) {
            if (rand.nextDouble() < 0.6) {
                // First cluster
                data[i][0] = rand.nextGaussian() * 0.5 + 2;
                data[i][1] = rand.nextGaussian() * 0.5 + 2;
            } else {
                // Second cluster
                data[i][0] = rand.nextGaussian() * 0.3 + 4;
                data[i][1] = rand.nextGaussian() * 0.3 + 4;
            }
        }

        // Calculate grid boundaries
        double minX = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;

        for (double[] point : data) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }

        // Add padding
        double padX = (maxX - minX) * 0.1;
        double padY = (maxY - minY) * 0.1;
        minX -= padX;
        maxX += padX;
        minY -= padY;
        maxY += padY;

        // Calculate bandwidth using Silverman's rule
        double sdX = calculateSD(data, 0);
        double sdY = calculateSD(data, 1);
        bandwidthX = 1.06 * sdX * Math.pow(n, -0.2);
        bandwidthY = 1.06 * sdY * Math.pow(n, -0.2);

        // Create KDE mapper
        Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
                double sum = 0;
                for (double[] point : data) {
                    double zx = (x - point[0]) / bandwidthX;
                    double zy = (y - point[1]) / bandwidthY;
                    sum += Math.exp(-0.5 * (zx * zx + zy * zy)) /
                      (2 * Math.PI * bandwidthX * bandwidthY);
                }
                return sum / data.length;
            }
        };

        // Create surface
        Range xRange = new Range((float)minX, (float)maxX);
        Range yRange = new Range((float)minY, (float)maxY);

        OrthonormalGrid grid = new OrthonormalGrid(xRange, gridSize, yRange, gridSize);
        Shape surface = new SurfaceBuilder().orthonormal(grid, mapper);

        // Style the surface
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax()));
        surface.setWireframeDisplayed(true);
        surface.setWireframeColor(Color.BLACK);
        chart.add(surface);

        // Create scatter plot of original data
        List<Coord3d> points = new ArrayList<>();
        for (double[] point : data) {
            points.add(new Coord3d(point[0], point[1], 0));
        }
        Scatter scatter = new Scatter(points.toArray(new Coord3d[0]), Color.BLACK);
        chart.add(scatter);
    }

    private static double calculateSD(double[][] data, int dimension) {
        double mean = 0;
        for (double[] point : data) {
            mean += point[dimension];
        }
        mean /= data.length;

        double variance = 0;
        for (double[] point : data) {
            double diff = point[dimension] - mean;
            variance += diff * diff;
        }
        variance /= (data.length - 1);

        return Math.sqrt(variance);
    }

    @Override
    public String getName() {
        return "Bivariate KDE Example";
    }

    @Override
    public String getPitch() {
        return "2D Kernel Density Estimation visualization";
    }
}
