package org.scijava.ui.swing.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

public class KDEContourPlot {
    public static void main(String[] args) {
        // Generate sample data
        double[][] data = generateSampleData();
        
        // Create and display the plot
        JFrame frame = new JFrame("2D KDE Contour Plot");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(createChartPanel(data));
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    private static class ContourDataset extends AbstractXYDataset {
        private final List<List<Point2D>> contourLines;
        private final List<Boolean> isFirstCluster; // Track which cluster each contour belongs to

        public ContourDataset(double[][] data, int gridSize, int numContours) {
            // Calculate KDE
            double[][] density = calculateKDE(data, gridSize);
            // Generate contour lines with cluster information
            var result = generateContours(density, numContours, data);
            this.contourLines = result.contourLines;
            this.isFirstCluster = result.isFirstCluster;
        }

        public boolean isFirstCluster(int series) {
            return isFirstCluster.get(series);
        }

        @Override
        public int getSeriesCount() {
            return contourLines.size();
        }
        
        @Override
        public Comparable getSeriesKey(int series) {
            return "Contour " + series;
        }
        
        @Override
        public int getItemCount(int series) {
            return contourLines.get(series).size();
        }
        
        @Override
        public Number getX(int series, int item) {
            return contourLines.get(series).get(item).getX();
        }
        
        @Override
        public Number getY(int series, int item) {
            return contourLines.get(series).get(item).getY();
        }
    }
    
    private static double[][] calculateKDE(double[][] data, int gridSize) {
        // Find data bounds
        double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;
        for (double[] point : data) {
            minX = Math.min(minX, point[0]);
            maxX = Math.max(maxX, point[0]);
            minY = Math.min(minY, point[1]);
            maxY = Math.max(maxY, point[1]);
        }
        
        // Add padding
        double padX = (maxX - minX) * 0.1;
        double padY = (maxY - minY) * 0.1;
        minX -= padX; maxX += padX;
        minY -= padY; maxY += padY;
        
        // Calculate bandwidth using Silverman's rule
        double sdX = calculateSD(data, 0);
        double sdY = calculateSD(data, 1);
        double n = data.length;
        double bandwidthX = 1.06 * sdX * Math.pow(n, -0.2);
        double bandwidthY = 1.06 * sdY * Math.pow(n, -0.2);
        
        // Calculate KDE on grid
        double[][] density = new double[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                double x = minX + (maxX - minX) * i / (gridSize - 1);
                double y = minY + (maxY - minY) * j / (gridSize - 1);
                
                double sum = 0;
                for (double[] point : data) {
                    double zx = (x - point[0]) / bandwidthX;
                    double zy = (y - point[1]) / bandwidthY;
                    sum += Math.exp(-0.5 * (zx * zx + zy * zy)) / 
                           (2 * Math.PI * bandwidthX * bandwidthY);
                }
                density[i][j] = sum / n;
            }
        }
        
        return density;
    }

    private static class ContourResult {
        List<List<Point2D>> contourLines;
        List<Boolean> isFirstCluster;

        ContourResult(List<List<Point2D>> contourLines, List<Boolean> isFirstCluster) {
            this.contourLines = contourLines;
            this.isFirstCluster = isFirstCluster;
        }
    }

    private static ContourResult generateContours(double[][] density, int numContours, double[][] originalData) {
        List<List<Point2D>> contourLines = new ArrayList<>();
        List<Boolean> isFirstCluster = new ArrayList<>();
        double maxDensity = Arrays.stream(density)
          .flatMapToDouble(Arrays::stream)
          .max()
          .orElse(1.0);

        // For each contour level
        for (int i = 1; i <= numContours; i++) {
            double level = maxDensity * i / (numContours + 1);
            List<Point2D> points = new ArrayList<>();

            // Collect all points for this contour
            for (int x = 0; x < density.length - 1; x++) {
                for (int y = 0; y < density[0].length - 1; y++) {
                    boolean bl = density[x][y] >= level;
                    boolean br = density[x+1][y] >= level;
                    boolean tr = density[x+1][y+1] >= level;
                    boolean tl = density[x][y+1] >= level;

                    int caseNum = (bl ? 1 : 0) + (br ? 2 : 0) +
                      (tr ? 4 : 0) + (tl ? 8 : 0);

                    if (caseNum != 0 && caseNum != 15) {
                        points.add(new Point2D.Double(x + 0.5, y + 0.5));
                    }
                }
            }

            if (!points.isEmpty()) {
                // Order points to form a continuous contour
                List<Point2D> orderedPoints = orderContourPoints(points);
                contourLines.add(orderedPoints);

                // Determine which cluster this contour belongs to
                Point2D centerPoint = findContourCenter(orderedPoints);
                boolean belongsToFirstCluster = determineCluster(centerPoint, originalData);
                isFirstCluster.add(belongsToFirstCluster);
            }
        }

        return new ContourResult(contourLines, isFirstCluster);
    }
    private static List<Point2D> orderContourPoints(List<Point2D> points) {
        List<Point2D> ordered = new ArrayList<>();
        Set<Point2D> remaining = new HashSet<>(points);

        // Start with the leftmost point
        Point2D current = points.stream()
          .min(Comparator.comparingDouble(Point2D::getX))
          .orElseThrow();
        ordered.add(current);
        remaining.remove(current);

        while (!remaining.isEmpty()) {
            Point2D finalCurrent = current;
            current = remaining.stream()
              .min(Comparator.comparingDouble(p ->
                finalCurrent.distance(p)))
              .orElseThrow();

            ordered.add(current);
            remaining.remove(current);
        }

        return ordered;
    }

    private static Point2D findContourCenter(List<Point2D> points) {
        double sumX = 0, sumY = 0;
        for (Point2D point : points) {
            sumX += point.getX();
            sumY += point.getY();
        }
        return new Point2D.Double(sumX / points.size(), sumY / points.size());
    }

    private static boolean determineCluster(Point2D point, double[][] originalData) {
        // Calculate distance to cluster centers
        double[] center1 = {2, 2}; // Approximate center of first cluster
        double[] center2 = {4, 4}; // Approximate center of second cluster

        double dist1 = Math.sqrt(Math.pow(point.getX() - center1[0], 2) +
          Math.pow(point.getY() - center1[1], 2));
        double dist2 = Math.sqrt(Math.pow(point.getX() - center2[0], 2) +
          Math.pow(point.getY() - center2[1], 2));

        return dist1 < dist2;
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
    
    private static double[][] generateSampleData() {
        Random rand = new Random(42);
        int n = 1000;
        double[][] data = new double[n][2];
        
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
        
        return data;
    }
    
    private static ChartPanel createChartPanel(double[][] data) {
        XYDataset dataset = new ContourDataset(data, 50, 8);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "2D KDE Contour Plot",
            "X",
            "Y",
            dataset
        );
        
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        
        // Style each contour line differently
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);
            float hue = (float)i / dataset.getSeriesCount();
            renderer.setSeriesPaint(i, Color.getHSBColor(hue, 0.8f, 0.8f));
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }
        
        plot.setRenderer(renderer);
        
        return new ChartPanel(chart);
    }
}