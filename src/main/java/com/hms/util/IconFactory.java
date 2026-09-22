
package com.hms.util;

import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.*;

/**
 * Programmatic vector icon engine built on JavaFX native Shapes
 * (Circle, Rectangle, Polygon, Line, Path), preventing missing assets
 * and low-DPI scaling blur.
 */
public final class IconFactory {

    private IconFactory() {
    }

    // ---- Modern Hospital & Clinical Logos ----------------------------

    /**
     * Modern clinical shield & cross logo.
     * Ideal for replacing outdated text headers in the sidebar and login cards.
     *
     * @param size Base width and height of the rendered logo
     * @param sidebarPalette true if rendering on dark green/teal sidebar; false for light surfaces
     */
    public static Group medicalShieldLogo(double size, boolean sidebarPalette) {
        // Base shield path (normalized to 100x100)
        SVGPath shield = new SVGPath();
        shield.setContent("M 50,6 " +
                "C 74,6 90,14 90,26 " +
                "C 90,56 74,82 50,94 " +
                "C 26,82 10,56 10,26 " +
                "C 10,14 26,6 50,6 Z");

        LinearGradient gradient;
        if (sidebarPalette) {
            gradient = new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#2dd4bf")),
                    new Stop(1, Color.web("#0d9488"))
            );
        } else {
            gradient = new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#14b8a6")),
                    new Stop(1, Color.web("#0f766e"))
            );
        }
        shield.setFill(gradient);

        // Inner medical cross (+)
        SVGPath cross = new SVGPath();
        cross.setContent("M 43,28 " +
                "L 57,28 A 3,3 0 0 1 60,31 L 60,43 L 72,43 A 3,3 0 0 1 75,46 L 75,54 A 3,3 0 0 1 72,57 L 60,57 L 60,69 A 3,3 0 0 1 57,72 L 43,72 A 3,3 0 0 1 40,69 L 40,57 L 28,57 A 3,3 0 0 1 25,54 L 25,46 A 3,3 0 0 1 28,43 L 40,43 L 40,31 A 3,3 0 0 1 43,28 Z");
        cross.setFill(Color.WHITE);

        Group group = new Group(shield, cross);
        double scale = size / 100.0;
        group.setScaleX(scale);
        group.setScaleY(scale);
        group.setTranslateX(-50 * (1 - scale));
        group.setTranslateY(-50 * (1 - scale));
        return group;
    }

    /**
     * Standalone modern hospital cross badge.
     */
    public static Group hospitalCrossBadge(double size) {
        Rectangle bg = new Rectangle(0, 0, size, size);
        bg.setArcWidth(size * 0.32);
        bg.setArcHeight(size * 0.32);
        bg.setFill(Color.web("#0f9d8c"));

        double barW = size * 0.22;
        double barL = size * 0.60;
        double offsetL = (size - barL) / 2.0;
        double offsetW = (size - barW) / 2.0;

        Rectangle hBar = new Rectangle(offsetL, offsetW, barL, barW);
        hBar.setArcWidth(size * 0.08);
        hBar.setArcHeight(size * 0.08);
        hBar.setFill(Color.WHITE);

        Rectangle vBar = new Rectangle(offsetW, offsetL, barW, barL);
        vBar.setArcWidth(size * 0.08);
        vBar.setArcHeight(size * 0.08);
        vBar.setFill(Color.WHITE);

        return new Group(bg, hBar, vBar);
    }

    // ---- Legacy Logo (Maintained for Backward Compatibility) ----------

    public static Group gearCrossLogo(double size, boolean sidebarPalette) {
        String bodyClass = sidebarPalette ? "logo-gear-teeth-sidebar" : "logo-gear-teeth";
        String boreClass = sidebarPalette ? "logo-gear-bore-sidebar" : "logo-gear-bore";

        Group gear = new Group();
        Circle body = new Circle(50, 50, 34);
        body.getStyleClass().add(bodyClass);
        gear.getChildren().add(body);

        int teethCount = 8;
        double toothWidth = 15;
        double toothHeight = 17;
        for (int i = 0; i < teethCount; i++) {
            Rectangle tooth = new Rectangle(50 - toothWidth / 2, 50 - 34 - toothHeight + 4, toothWidth, toothHeight);
            tooth.setArcWidth(3);
            tooth.setArcHeight(3);
            tooth.getStyleClass().add(bodyClass);
            tooth.getTransforms().add(new javafx.scene.transform.Rotate(i * (360.0 / teethCount), 50, 50));
            gear.getChildren().add(tooth);
        }

        Circle bore = new Circle(50, 50, 15);
        bore.getStyleClass().add(boreClass);

        Rectangle crossOutlineH = new Rectangle(22, 41, 66, 18);
        crossOutlineH.setArcWidth(6);
        crossOutlineH.setArcHeight(6);
        crossOutlineH.getStyleClass().add("logo-cross-outline");

        Rectangle crossOutlineV = new Rectangle(41, 22, 18, 66);
        crossOutlineV.setArcWidth(6);
        crossOutlineV.setArcHeight(6);
        crossOutlineV.getStyleClass().add("logo-cross-outline");

        Rectangle crossH = new Rectangle(24, 43, 62, 14);
        crossH.setArcWidth(5);
        crossH.setArcHeight(5);
        crossH.getStyleClass().add("logo-cross");

        Rectangle crossV = new Rectangle(43, 24, 14, 62);
        crossV.setArcWidth(5);
        crossV.setArcHeight(5);
        crossV.getStyleClass().add("logo-cross");

        Group group = new Group(gear, bore, crossOutlineH, crossOutlineV, crossH, crossV);
        double scale = size / 100.0;
        group.setScaleX(scale);
        group.setScaleY(scale);
        group.setTranslateX(-50 * (1 - scale));
        group.setTranslateY(-50 * (1 - scale));
        return group;
    }

    // ---- Sidebar Navigation Icons ------------------------------------

    public static Group grid(double size, String styleClass) {
        double cell = size * 0.42;
        double gap = size * 0.16;
        Group g = new Group();
        double[][] positions = {{0, 0}, {cell + gap, 0}, {0, cell + gap}, {cell + gap, cell + gap}};
        for (double[] pos : positions) {
            Rectangle r = new Rectangle(pos[0], pos[1], cell, cell);
            r.setArcWidth(3);
            r.setArcHeight(3);
            r.getStyleClass().add(styleClass);
            g.getChildren().add(r);
        }
        return g;
    }

    public static Group person(double size, String styleClass) {
        Circle head = new Circle(size * 0.5, size * 0.32, size * 0.16);
        head.getStyleClass().add(styleClass);
        Circle shoulders = new Circle(size * 0.5, size * 0.98, size * 0.3);
        shoulders.getStyleClass().add(styleClass);
        Group g = new Group(head, shoulders);
        g.setClip(new Rectangle(0, 0, size, size * 0.98));
        return g;
    }

    public static Group personOutline(double size, String styleClass) {
        Circle head = new Circle(size * 0.5, size * 0.32, size * 0.16);
        head.setFill(javafx.scene.paint.Color.TRANSPARENT);
        head.getStyleClass().addAll(styleClass, "icon-stroke");
        Circle shoulders = new Circle(size * 0.5, size * 0.98, size * 0.3);
        shoulders.setFill(javafx.scene.paint.Color.TRANSPARENT);
        shoulders.getStyleClass().addAll(styleClass, "icon-stroke");
        Group g = new Group(head, shoulders);
        g.setClip(new Rectangle(0, 0, size, size * 0.98));
        return g;
    }

    public static Group doctorPerson(double size, String styleClass, String badgeStyleClass) {
        Group base = person(size, styleClass);
        Circle badgeCircle = new Circle(size * 0.82, size * 0.78, size * 0.2);
        badgeCircle.getStyleClass().add(badgeStyleClass);
        Rectangle h = new Rectangle(size * 0.74, size * 0.755, size * 0.16, size * 0.05);
        h.getStyleClass().add(styleClass);
        Rectangle v = new Rectangle(size * 0.795, size * 0.70, size * 0.05, size * 0.16);
        v.getStyleClass().add(styleClass);
        return new Group(base, badgeCircle, h, v);
    }

    public static Group doctorPersonOutline(double size, String styleClass) {
        Group base = personOutline(size, styleClass);
        Circle badgeCircle = new Circle(size * 0.82, size * 0.78, size * 0.16);
        badgeCircle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        badgeCircle.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(base, badgeCircle);
    }

    public static Group calendar(double size, String styleClass) {
        Rectangle body = new Rectangle(size * 0.08, size * 0.16, size * 0.84, size * 0.76);
        body.setArcWidth(size * 0.1);
        body.setArcHeight(size * 0.1);
        body.setFill(javafx.scene.paint.Color.TRANSPARENT);
        body.getStyleClass().addAll(styleClass, "icon-stroke");

        Line header = new Line(size * 0.08, size * 0.38, size * 0.92, size * 0.38);
        header.getStyleClass().addAll(styleClass, "icon-stroke");

        Rectangle tabLeft = new Rectangle(size * 0.24, size * 0.06, size * 0.08, size * 0.2);
        tabLeft.setArcWidth(size * 0.04);
        tabLeft.setArcHeight(size * 0.04);
        tabLeft.getStyleClass().add(styleClass);

        Rectangle tabRight = new Rectangle(size * 0.68, size * 0.06, size * 0.08, size * 0.2);
        tabRight.setArcWidth(size * 0.04);
        tabRight.setArcHeight(size * 0.04);
        tabRight.getStyleClass().add(styleClass);

        Rectangle dateMark = new Rectangle(size * 0.36, size * 0.54, size * 0.28, size * 0.16);
        dateMark.setArcWidth(size * 0.05);
        dateMark.setArcHeight(size * 0.05);
        dateMark.getStyleClass().add(styleClass);

        return new Group(body, header, tabLeft, tabRight, dateMark);
    }

    public static Group bed(double size, String styleClass) {
        Rectangle mattress = new Rectangle(size * 0.06, size * 0.5, size * 0.88, size * 0.3);
        mattress.setArcWidth(size * 0.08);
        mattress.setArcHeight(size * 0.08);
        mattress.getStyleClass().add(styleClass);

        Rectangle headboard = new Rectangle(size * 0.06, size * 0.22, size * 0.16, size * 0.6);
        headboard.setArcWidth(size * 0.06);
        headboard.setArcHeight(size * 0.06);
        headboard.getStyleClass().add(styleClass);

        Line leg1 = new Line(size * 0.12, size * 0.8, size * 0.12, size * 0.94);
        leg1.getStyleClass().addAll(styleClass, "icon-stroke");
        Line leg2 = new Line(size * 0.86, size * 0.8, size * 0.86, size * 0.94);
        leg2.getStyleClass().addAll(styleClass, "icon-stroke");

        return new Group(mattress, headboard, leg1, leg2);
    }

    public static Group clipboard(double size, String styleClass) {
        Rectangle board = new Rectangle(size * 0.14, size * 0.14, size * 0.72, size * 0.8);
        board.setArcWidth(size * 0.08);
        board.setArcHeight(size * 0.08);
        board.setFill(javafx.scene.paint.Color.TRANSPARENT);
        board.getStyleClass().addAll(styleClass, "icon-stroke");

        Rectangle clip = new Rectangle(size * 0.36, size * 0.04, size * 0.28, size * 0.16);
        clip.setArcWidth(size * 0.06);
        clip.setArcHeight(size * 0.06);
        clip.getStyleClass().add(styleClass);

        Group lines = new Group();
        for (int i = 0; i < 3; i++) {
            double y = size * (0.38 + i * 0.16);
            Line line = new Line(size * 0.26, y, size * 0.74, y);
            line.getStyleClass().addAll(styleClass, "icon-stroke");
            lines.getChildren().add(line);
        }

        return new Group(board, clip, lines);
    }

    public static Group barChart(double size, String styleClass) {
        Rectangle bar1 = new Rectangle(size * 0.12, size * 0.55, size * 0.2, size * 0.35);
        Rectangle bar2 = new Rectangle(size * 0.4, size * 0.35, size * 0.2, size * 0.55);
        Rectangle bar3 = new Rectangle(size * 0.68, size * 0.15, size * 0.2, size * 0.75);
        for (Rectangle r : new Rectangle[]{bar1, bar2, bar3}) {
            r.setArcWidth(size * 0.05);
            r.setArcHeight(size * 0.05);
            r.getStyleClass().add(styleClass);
        }
        return new Group(bar1, bar2, bar3);
    }

    public static Group shield(double size, String styleClass) {
        Polygon shield = new Polygon(
                size * 0.5, size * 0.04,
                size * 0.88, size * 0.18,
                size * 0.88, size * 0.5,
                size * 0.5, size * 0.96,
                size * 0.12, size * 0.5,
                size * 0.12, size * 0.18
        );
        shield.setFill(javafx.scene.paint.Color.TRANSPARENT);
        shield.getStyleClass().addAll(styleClass, "icon-stroke");

        Line checkA = new Line(size * 0.34, size * 0.5, size * 0.46, size * 0.62);
        Line checkB = new Line(size * 0.46, size * 0.62, size * 0.68, size * 0.36);
        checkA.getStyleClass().addAll(styleClass, "icon-stroke");
        checkB.getStyleClass().addAll(styleClass, "icon-stroke");

        return new Group(shield, checkA, checkB);
    }

    public static Group cash(double size, String styleClass) {
        Circle coin1 = new Circle(size * 0.36, size * 0.64, size * 0.3);
        Circle coin2 = new Circle(size * 0.64, size * 0.4, size * 0.3);
        coin1.setFill(javafx.scene.paint.Color.TRANSPARENT);
        coin2.setFill(javafx.scene.paint.Color.TRANSPARENT);
        coin1.getStyleClass().addAll(styleClass, "icon-stroke");
        coin2.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(coin1, coin2);
    }

    public static Group logout(double size, String styleClass) {
        Rectangle door = new Rectangle(size * 0.1, size * 0.1, size * 0.32, size * 0.8);
        door.setArcWidth(size * 0.06);
        door.setArcHeight(size * 0.06);
        door.setFill(javafx.scene.paint.Color.TRANSPARENT);
        door.getStyleClass().addAll(styleClass, "icon-stroke");

        Line shaft = new Line(size * 0.42, size * 0.5, size * 0.86, size * 0.5);
        shaft.getStyleClass().addAll(styleClass, "icon-stroke");

        Polygon arrowHead = new Polygon(
                size * 0.68, size * 0.34,
                size * 0.9, size * 0.5,
                size * 0.68, size * 0.66
        );
        arrowHead.setFill(javafx.scene.paint.Color.TRANSPARENT);
        arrowHead.getStyleClass().addAll(styleClass, "icon-stroke");

        return new Group(door, shaft, arrowHead);
    }

    // ---- Top Bar & UI Icons -------------------------------------------

    public static Group search(double size, String styleClass) {
        Circle lens = new Circle(size * 0.4, size * 0.4, size * 0.3);
        lens.setFill(javafx.scene.paint.Color.TRANSPARENT);
        lens.getStyleClass().addAll(styleClass, "icon-stroke");
        Line handle = new Line(size * 0.62, size * 0.62, size * 0.9, size * 0.9);
        handle.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(lens, handle);
    }

    public static Group bell(double size, String styleClass) {
        Polygon body = new Polygon(
                size * 0.5, size * 0.06,
                size * 0.82, size * 0.62,
                size * 0.18, size * 0.62
        );
        body.getStyleClass().add(styleClass);
        Rectangle band = new Rectangle(size * 0.14, size * 0.62, size * 0.72, size * 0.1);
        band.setArcWidth(size * 0.05);
        band.setArcHeight(size * 0.05);
        band.getStyleClass().add(styleClass);
        Circle clapper = new Circle(size * 0.5, size * 0.86, size * 0.08);
        clapper.getStyleClass().add(styleClass);
        return new Group(body, band, clapper);
    }

    public static Group sun(double size, String styleClass) {
        Group g = new Group();
        Circle core = new Circle(size * 0.5, size * 0.5, size * 0.22);
        core.getStyleClass().add(styleClass);
        g.getChildren().add(core);
        for (int i = 0; i < 8; i++) {
            double angle = Math.toRadians(i * 45);
            double x1 = size * 0.5 + Math.cos(angle) * size * 0.32;
            double y1 = size * 0.5 + Math.sin(angle) * size * 0.32;
            double x2 = size * 0.5 + Math.cos(angle) * size * 0.46;
            double y2 = size * 0.5 + Math.sin(angle) * size * 0.46;
            Line ray = new Line(x1, y1, x2, y2);
            ray.getStyleClass().addAll(styleClass, "icon-stroke");
            g.getChildren().add(ray);
        }
        return g;
    }

    public static Group moon(double size, String styleClass) {
        Circle full = new Circle(size * 0.5, size * 0.5, size * 0.34);
        full.getStyleClass().add(styleClass);
        Circle bite = new Circle(size * 0.66, size * 0.38, size * 0.28);
        bite.getStyleClass().add("moon-bite");
        return new Group(full, bite);
    }

    public static Group eye(double size, String styleClass) {
        double w = size * 1.3;
        Polygon outline = new Polygon(
                0, size * 0.5,
                w * 0.5, 0,
                w, size * 0.5,
                w * 0.5, size
        );
        outline.setFill(javafx.scene.paint.Color.TRANSPARENT);
        outline.getStyleClass().addAll(styleClass, "icon-stroke");
        Circle pupil = new Circle(w * 0.5, size * 0.5, size * 0.22);
        pupil.getStyleClass().add(styleClass);
        return new Group(outline, pupil);
    }

    public static Group eyeSlash(double size, String styleClass) {
        Group base = eye(size, styleClass);
        double w = size * 1.3;
        Line slash = new Line(0, size, w, 0);
        slash.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(base, slash);
    }

    public static Group chevronLeft(double size, String styleClass) {
        Polygon p = new Polygon(
                size * 0.65, size * 0.1,
                size * 0.3, size * 0.5,
                size * 0.65, size * 0.9
        );
        p.setFill(javafx.scene.paint.Color.TRANSPARENT);
        p.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(p);
    }

    public static Group chevronRight(double size, String styleClass) {
        Polygon p = new Polygon(
                size * 0.35, size * 0.1,
                size * 0.7, size * 0.5,
                size * 0.35, size * 0.9
        );
        p.setFill(javafx.scene.paint.Color.TRANSPARENT);
        p.getStyleClass().addAll(styleClass, "icon-stroke");
        return new Group(p);
    }
}