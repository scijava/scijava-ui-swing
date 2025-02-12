package org.scijava.ui.swing.plot;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.AbstractXYDataset;

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
        private final List<Boolean> isFirstCluster;

        public ContourDataset(double[][] data, int gridSize, int numContours) {
            double[][] density = calculateKDE(data, gridSize);
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

    private static Point2D interpolate(double x1, double y1, double v1,
                                       double x2, double y2, double v2,
                                       double level) {
        if (Math.abs(v1 - v2) < 1e-10) {
            return new Point2D.Double(x1, y1);
        }
        double t = (level - v1) / (v2 - v1);
        return new Point2D.Double(
          x1 + t * (x2 - x1),
          y1 + t * (y2 - y1)
        );
    }

    private static ContourResult generateContours(double[][] density, int numContours, double[][] originalData) {
        List<List<Point2D>> allContourLines = new ArrayList<>();
        List<Boolean> isFirstCluster = new ArrayList<>();
        double maxDensity = Arrays.stream(density)
          .flatMapToDouble(Arrays::stream)
          .max()
          .orElse(1.0);

        // For each contour level
        for (int i = 1; i <= numContours; i++) {
            double level = maxDensity * i / (numContours + 1);
            List<List<Point2D>> contoursAtLevel = new ArrayList<>();

            boolean[][] visited = new boolean[density.length][density[0].length];

            // Scan for contour starting points
            for (int x = 0; x < density.length - 1; x++) {
                for (int y = 0; y < density[0].length - 1; y++) {
                    if (!visited[x][y]) {
                        List<Point2D> contour = traceContour(density, level, x, y, visited);
                        if (!contour.isEmpty()) {
                            contoursAtLevel.add(contour);
                        }
                    }
                }
            }

            // Process each separate contour at this level
            for (List<Point2D> contour : contoursAtLevel) {
                if (contour.size() >= 4) {  // Filter out tiny contours
                    Point2D centerPoint = findContourCenter(contour);
                    boolean belongsToFirstCluster = determineCluster(centerPoint, originalData);

                    allContourLines.add(contour);
                    isFirstCluster.add(belongsToFirstCluster);
                }
            }
        }

        return new ContourResult(allContourLines, isFirstCluster);
    }
    private static List<Point2D> traceContour(double[][] density, double level,
                                              int startX, int startY,
                                              boolean[][] visited) {
        List<Point2D> contour = new ArrayList<>();
        Queue<Point2D> queue = new LinkedList<>();
        Set<String> visitedEdges = new HashSet<>();

        // Initialize with start point
        addContourSegments(density, level, startX, startY, queue, visitedEdges);

        while (!queue.isEmpty()) {
            Point2D point = queue.poll();
            contour.add(point);

            // Find grid cell containing this point
            int x = (int) Math.floor(point.getX());
            int y = (int) Math.floor(point.getY());

            // Mark as visited
            if (x >= 0 && x < visited.length - 1 &&
              y >= 0 && y < visited[0].length - 1) {
                visited[x][y] = true;

                // Add adjacent segments
                addContourSegments(density, level, x, y, queue, visitedEdges);
            }
        }

        return contour;
    }

    private static void addContourSegments(double[][] density, double level,
                                           int x, int y,
                                           Queue<Point2D> queue,
                                           Set<String> visitedEdges) {
        if (x < 0 || x >= density.length - 1 ||
          y < 0 || y >= density[0].length - 1) {
            return;
        }

        double v00 = density[x][y];
        double v10 = density[x+1][y];
        double v11 = density[x+1][y+1];
        double v01 = density[x][y+1];

        // For each edge of the cell
        List<Point2D> intersections = new ArrayList<>();

        // Bottom edge
        if ((v00 < level && v10 >= level) || (v00 >= level && v10 < level)) {
            String edge = String.format("%d,%d,B", x, y);
            if (!visitedEdges.contains(edge)) {
                intersections.add(interpolate(x, y, v00, x+1, y, v10, level));
                visitedEdges.add(edge);
            }
        }

        // Right edge
        if ((v10 < level && v11 >= level) || (v10 >= level && v11 < level)) {
            String edge = String.format("%d,%d,R", x+1, y);
            if (!visitedEdges.contains(edge)) {
                intersections.add(interpolate(x+1, y, v10, x+1, y+1, v11, level));
                visitedEdges.add(edge);
            }
        }

        // Top edge
        if ((v01 < level && v11 >= level) || (v01 >= level && v11 < level)) {
            String edge = String.format("%d,%d,T", x, y+1);
            if (!visitedEdges.contains(edge)) {
                intersections.add(interpolate(x, y+1, v01, x+1, y+1, v11, level));
                visitedEdges.add(edge);
            }
        }

        // Left edge
        if ((v00 < level && v01 >= level) || (v00 >= level && v01 < level)) {
            String edge = String.format("%d,%d,L", x, y);
            if (!visitedEdges.contains(edge)) {
                intersections.add(interpolate(x, y, v00, x, y+1, v01, level));
                visitedEdges.add(edge);
            }
        }

        // Add all found intersections to the queue
        queue.addAll(intersections);
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
        ContourDataset dataset = new ContourDataset(data, 50, 8);

        JFreeChart chart = ChartFactory.createXYLineChart(
          "2D KDE Contour Plot",
          "X",
          "Y",
          dataset
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

        // Style contours based on cluster
        Color cluster1Color = Color.BLUE;
        Color cluster2Color = Color.ORANGE;

        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesLinesVisible(i, true);
            renderer.setSeriesShapesVisible(i, false);
            renderer.setSeriesPaint(i, dataset.isFirstCluster(i) ?
              cluster1Color : cluster2Color);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
        }

        plot.setRenderer(renderer);

        return new ChartPanel(chart);
    }
}
