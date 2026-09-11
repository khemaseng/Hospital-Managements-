package com.hms.view;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;

/**
 * Paints the login screen's "hospital campus blueprint" background:
 * a dark slate gradient, a technical grid overlay, and a handful of
 * simplified isometric building blocks with small accent icons (a
 * medical cross, a bed, a flask), evoking a hospital site map without
 * needing an external image asset - the whole thing redraws cleanly at
 * any window size.
 */
public final class BlueprintBackgroundPainter {

    private BlueprintBackgroundPainter() {
    }

    public static void paint(GraphicsContext gc, double width, double height) {
        gc.clearRect(0, 0, width, height);
        paintBaseGradient(gc, width, height);
        paintGrid(gc, width, height);
        paintBuildings(gc, width, height);
    }

    private static void paintBaseGradient(GraphicsContext gc, double width, double height) {
        LinearGradient gradient = new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.web("#2f4457")),
                new Stop(1, Color.web("#1b2b38")));
        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);
    }

    private static void paintGrid(GraphicsContext gc, double width, double height) {
        gc.setStroke(Color.web("#ffffff", 0.05));
        gc.setLineWidth(1);
        double spacing = 40;
        for (double x = 0; x <= width; x += spacing) {
            gc.strokeLine(x, 0, x, height);
        }
        for (double y = 0; y <= height; y += spacing) {
            gc.strokeLine(0, y, width, y);
        }
    }

    /**
     * Scatters a fixed (not random-per-frame) layout of isometric building
     * blocks around the canvas, scaled proportionally to its size, so the
     * "campus" composition stays stable across resizes.
     */
    private static void paintBuildings(GraphicsContext gc, double width, double height) {
        // Each entry: relative X, relative Y (0..1 of canvas), half-width, height, accent
        Object[][] layout = {
                {0.06, 0.15, 46.0, 70.0, "cross"},
                {0.16, 0.55, 60.0, 90.0, "bed"},
                {0.05, 0.80, 38.0, 55.0, "none"},
                {0.90, 0.12, 50.0, 75.0, "flask"},
                {0.80, 0.45, 42.0, 60.0, "none"},
                {0.92, 0.75, 55.0, 85.0, "cross"},
                {0.72, 0.85, 36.0, 50.0, "bed"},
        };

        for (Object[] b : layout) {
            double cx = ((Double) b[0]) * width;
            double cy = ((Double) b[1]) * height;
            double halfWidth = (Double) b[2];
            double blockHeight = (Double) b[3];
            String accent = (String) b[4];
            drawIsometricBlock(gc, cx, cy, halfWidth, blockHeight);
            switch (accent) {
                case "cross" -> drawCrossAccent(gc, cx, cy - blockHeight * 0.55);
                case "bed" -> drawBedAccent(gc, cx, cy - blockHeight * 0.55);
                case "flask" -> drawFlaskAccent(gc, cx, cy - blockHeight * 0.55);
                default -> { /* plain block, no accent */ }
            }
        }
    }

    /** A simple isometric "cube" (top/left/right faces) reads as a stylized building block. */
    private static void drawIsometricBlock(GraphicsContext gc, double cx, double cy, double halfWidth, double h) {
        double top = cy - halfWidth * 0.5;
        double right = cx + halfWidth;
        double bottom = cy + halfWidth * 0.5;
        double left = cx - halfWidth;

        // Top face
        gc.setFill(Color.web("#5c7691", 0.55));
        gc.fillPolygon(new double[]{cx, right, cx, left}, new double[]{top, cy, bottom, cy}, 4);

        // Left face
        gc.setFill(Color.web("#3c5268", 0.55));
        gc.fillPolygon(new double[]{left, cx, cx, left},
                new double[]{cy, bottom, bottom + h, cy + h}, 4);

        // Right face
        gc.setFill(Color.web("#28394a", 0.55));
        gc.fillPolygon(new double[]{cx, right, right, cx},
                new double[]{bottom, cy, cy + h, bottom + h}, 4);

        // Faint outline for a technical/blueprint feel
        gc.setStroke(Color.web("#ffffff", 0.12));
        gc.setLineWidth(1);
        gc.strokePolygon(new double[]{cx, right, cx, left}, new double[]{top, cy, bottom, cy}, 4);
    }

    private static void drawCrossAccent(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.web("#2fb5a3", 0.75));
        gc.fillRoundRect(cx - 9, cy - 3, 18, 6, 2, 2);
        gc.fillRoundRect(cx - 3, cy - 9, 6, 18, 2, 2);
    }

    private static void drawBedAccent(GraphicsContext gc, double cx, double cy) {
        gc.setFill(Color.web("#d4ece8", 0.65));
        gc.fillRoundRect(cx - 14, cy - 4, 28, 10, 3, 3);
        gc.fillRoundRect(cx - 14, cy - 10, 8, 10, 2, 2);
    }

    private static void drawFlaskAccent(GraphicsContext gc, double cx, double cy) {
        gc.setStroke(Color.web("#d4ece8", 0.65));
        gc.setLineWidth(2);
        gc.strokeLine(cx - 4, cy - 10, cx - 4, cy - 2);
        gc.strokeLine(cx + 4, cy - 10, cx + 4, cy - 2);
        gc.strokePolyline(new double[]{cx - 4, cx - 9, cx + 9, cx + 4},
                new double[]{cy - 2, cy + 8, cy + 8, cy - 2}, 4);
    }
}
