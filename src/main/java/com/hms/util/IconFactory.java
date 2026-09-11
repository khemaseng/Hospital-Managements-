package com.hms.util;

import javafx.scene.Group;
import javafx.scene.shape.*;

/**
 * Every icon in this application is built here as plain JavaFX Shapes
 * (Circle/Rectangle/Polygon/Line), not emoji or an image font.
 *
 * Why: emoji glyphs were confirmed (via real screenshots on the actual
 * target machine, not just "no exception thrown") to render as blank boxes
 * in JavaFX in this project. Vector shapes render identically on every
 * platform/JVM because they don't depend on any font having the right
 * glyph - the same reasoning already applied to the login screen's logo
 * and field icons is now applied everywhere else.
 *
 * Every method returns a small Group sized to fit within roughly
 * `size` x `size` and colored via a CSS style class (see the
 * ".icon-shape-*" rules in application.css), so callers control both size
 * and theme-appropriate color without touching this class.
 */
public final class IconFactory {

    private IconFactory() {
    }

    // ---- Logo -------------------------------------------------------

    /**
     * The gear + medical-cross brand mark: a solid circular gear body with
     * individually placed and rotated rectangular teeth (far more robust
     * than a single hand-computed gear polygon, which is prone to subtle
     * winding-order bugs), plus a large teal cross overlaid across the
     * center with arms extending past the gear's own edge - matching the
     * reference mark. Built at a 100-unit reference size and scaled to the
     * requested size via Group scale.
     */
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
            // Each tooth is drawn pointing "up" from the hub, centered on
            // the vertical axis through (50,50), then rotated into place
            // around that same center point via an explicit pivot - using
            // Node.setRotate() alone would pivot around the tooth's own
            // (very different) local bounds center and scatter the teeth.
            Rectangle tooth = new Rectangle(50 - toothWidth / 2, 50 - 34 - toothHeight + 4, toothWidth, toothHeight);
            tooth.setArcWidth(3);
            tooth.setArcHeight(3);
            tooth.getStyleClass().add(bodyClass);
            tooth.getTransforms().add(new javafx.scene.transform.Rotate(i * (360.0 / teethCount), 50, 50));
            gear.getChildren().add(tooth);
        }

        Circle bore = new Circle(50, 50, 15);
        bore.getStyleClass().add(boreClass);

        // Large teal cross, centered on the gear, arms extending past its
        // outer edge - the dominant element of the mark, matching the
        // reference logo rather than a small corner badge.
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

    // ---- Sidebar navigation icons ------------------------------------

    /** Dashboard: 2x2 grid of small squares. */
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

    /** Generic person silhouette: circle head + rounded shoulders, clipped to a square viewport. */
    public static Group person(double size, String styleClass) {
        Circle head = new Circle(size * 0.5, size * 0.32, size * 0.16);
        head.getStyleClass().add(styleClass);
        Circle shoulders = new Circle(size * 0.5, size * 0.98, size * 0.3);
        shoulders.getStyleClass().add(styleClass);
        Group g = new Group(head, shoulders);
        g.setClip(new Rectangle(0, 0, size, size * 0.98));
        return g;
    }

    /**
     * Outline (stroke-only) version of the person silhouette - used for
     * large low-opacity watermark icons, where a filled shape reads as a
     * heavy "blob" but a thin outline reads as clean, deliberate line art.
     */
    public static Group personOutline(double size, String styleClass) {
        Circle head = new Circle(size * 0.5, size * 0.32, size * 0.16);
        head.setFill(javafx.scene.paint.Color.TRANSPARENT);
        head.getStyleClass().add(styleClass + "-stroke");
        Circle shoulders = new Circle(size * 0.5, size * 0.98, size * 0.3);
        shoulders.setFill(javafx.scene.paint.Color.TRANSPARENT);
        shoulders.getStyleClass().add(styleClass + "-stroke");
        Group g = new Group(head, shoulders);
        g.setClip(new Rectangle(0, 0, size, size * 0.98));
        return g;
    }

    /** Doctor: a person silhouette plus a small medical-cross badge, distinguishing it from a plain person icon. */
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

    /** Outline version of doctorPerson, for watermark use (see personOutline). */
    public static Group doctorPersonOutline(double size, String styleClass) {
        Group base = personOutline(size, styleClass);
        Circle badgeCircle = new Circle(size * 0.82, size * 0.78, size * 0.16);
        badgeCircle.setFill(javafx.scene.paint.Color.TRANSPARENT);
        badgeCircle.getStyleClass().add(styleClass + "-stroke");
        return new Group(base, badgeCircle);
    }

    /** Appointments: a calendar with a header band and a highlighted date. */
    public static Group calendar(double size, String styleClass) {
        Rectangle body = new Rectangle(size * 0.08, size * 0.16, size * 0.84, size * 0.76);
        body.setArcWidth(size * 0.1);
        body.setArcHeight(size * 0.1);
        body.setFill(javafx.scene.paint.Color.TRANSPARENT);
        body.getStyleClass().add(styleClass + "-stroke");

        Line header = new Line(size * 0.08, size * 0.38, size * 0.92, size * 0.38);
        header.getStyleClass().add(styleClass + "-stroke");

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

    /** Rooms/Wards: a simple bed - a headboard plus a mattress and legs. */
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
        leg1.getStyleClass().add(styleClass + "-stroke");
        Line leg2 = new Line(size * 0.86, size * 0.8, size * 0.86, size * 0.94);
        leg2.getStyleClass().add(styleClass + "-stroke");

        return new Group(mattress, headboard, leg1, leg2);
    }

    /** Medical Records: a clipboard with a clip and ruled lines. */
    public static Group clipboard(double size, String styleClass) {
        Rectangle board = new Rectangle(size * 0.14, size * 0.14, size * 0.72, size * 0.8);
        board.setArcWidth(size * 0.08);
        board.setArcHeight(size * 0.08);
        board.setFill(javafx.scene.paint.Color.TRANSPARENT);
        board.getStyleClass().add(styleClass + "-stroke");

        Rectangle clip = new Rectangle(size * 0.36, size * 0.04, size * 0.28, size * 0.16);
        clip.setArcWidth(size * 0.06);
        clip.setArcHeight(size * 0.06);
        clip.getStyleClass().add(styleClass);

        Group lines = new Group();
        for (int i = 0; i < 3; i++) {
            double y = size * (0.38 + i * 0.16);
            Line line = new Line(size * 0.26, y, size * 0.74, y);
            line.getStyleClass().add(styleClass + "-stroke");
            lines.getChildren().add(line);
        }

        return new Group(board, clip, lines);
    }

    /** Reports: a simple ascending bar chart. */
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

    /** Audit Log: a shield outline with a checkmark. */
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
        shield.getStyleClass().add(styleClass + "-stroke");

        Line checkA = new Line(size * 0.34, size * 0.5, size * 0.46, size * 0.62);
        Line checkB = new Line(size * 0.46, size * 0.62, size * 0.68, size * 0.36);
        checkA.getStyleClass().add(styleClass + "-stroke");
        checkB.getStyleClass().add(styleClass + "-stroke");

        return new Group(shield, checkA, checkB);
    }

    /** Billing: two overlapping coins. */
    public static Group cash(double size, String styleClass) {
        Circle coin1 = new Circle(size * 0.36, size * 0.64, size * 0.3);
        Circle coin2 = new Circle(size * 0.64, size * 0.4, size * 0.3);
        coin1.setFill(javafx.scene.paint.Color.TRANSPARENT);
        coin2.setFill(javafx.scene.paint.Color.TRANSPARENT);
        coin1.getStyleClass().add(styleClass + "-stroke");
        coin2.getStyleClass().add(styleClass + "-stroke");
        return new Group(coin1, coin2);
    }

    /** Logout: an open door with an arrow pointing out of it. */
    public static Group logout(double size, String styleClass) {
        Rectangle door = new Rectangle(size * 0.1, size * 0.1, size * 0.32, size * 0.8);
        door.setArcWidth(size * 0.06);
        door.setArcHeight(size * 0.06);
        door.setFill(javafx.scene.paint.Color.TRANSPARENT);
        door.getStyleClass().add(styleClass + "-stroke");

        Line shaft = new Line(size * 0.42, size * 0.5, size * 0.86, size * 0.5);
        shaft.getStyleClass().add(styleClass + "-stroke");

        Polygon arrowHead = new Polygon(
                size * 0.68, size * 0.34,
                size * 0.9, size * 0.5,
                size * 0.68, size * 0.66
        );
        arrowHead.setFill(javafx.scene.paint.Color.TRANSPARENT);
        arrowHead.getStyleClass().add(styleClass + "-stroke");

        return new Group(door, shaft, arrowHead);
    }

    // ---- Top bar icons ------------------------------------------------

    public static Group search(double size, String styleClass) {
        Circle lens = new Circle(size * 0.4, size * 0.4, size * 0.3);
        lens.setFill(javafx.scene.paint.Color.TRANSPARENT);
        lens.getStyleClass().add(styleClass + "-stroke");
        Line handle = new Line(size * 0.62, size * 0.62, size * 0.9, size * 0.9);
        handle.getStyleClass().add(styleClass + "-stroke");
        return new Group(lens, handle);
    }

    /** Notification bell. */
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
            ray.getStyleClass().add(styleClass + "-stroke");
            g.getChildren().add(ray);
        }
        return g;
    }

    /** Moon: a circle with a smaller offset circle painted to match the surrounding surface, creating a crescent. */
    public static Group moon(double size, String styleClass) {
        Circle full = new Circle(size * 0.5, size * 0.5, size * 0.34);
        full.getStyleClass().add(styleClass);
        Circle bite = new Circle(size * 0.66, size * 0.38, size * 0.28);
        bite.getStyleClass().add("moon-bite");
        return new Group(full, bite);
    }

    /** Open eye - password visible. */
    public static Group eye(double size, String styleClass) {
        double w = size * 1.3;
        Polygon outline = new Polygon(
                0, size * 0.5,
                w * 0.5, 0,
                w, size * 0.5,
                w * 0.5, size
        );
        outline.setFill(javafx.scene.paint.Color.TRANSPARENT);
        outline.getStyleClass().add(styleClass + "-stroke");
        Circle pupil = new Circle(w * 0.5, size * 0.5, size * 0.22);
        pupil.getStyleClass().add(styleClass);
        return new Group(outline, pupil);
    }

    /** Closed eye (with a diagonal slash) - password hidden. */
    public static Group eyeSlash(double size, String styleClass) {
        Group base = eye(size, styleClass);
        double w = size * 1.3;
        Line slash = new Line(0, size, w, 0);
        slash.getStyleClass().add(styleClass + "-stroke");
        return new Group(base, slash);
    }

    public static Group chevronLeft(double size, String styleClass) {
        Polygon p = new Polygon(
                size * 0.65, size * 0.1,
                size * 0.3, size * 0.5,
                size * 0.65, size * 0.9
        );
        p.setFill(javafx.scene.paint.Color.TRANSPARENT);
        p.getStyleClass().add(styleClass + "-stroke");
        return new Group(p);
    }

    public static Group chevronRight(double size, String styleClass) {
        Polygon p = new Polygon(
                size * 0.35, size * 0.1,
                size * 0.7, size * 0.5,
                size * 0.35, size * 0.9
        );
        p.setFill(javafx.scene.paint.Color.TRANSPARENT);
        p.getStyleClass().add(styleClass + "-stroke");
        return new Group(p);
    }
}
