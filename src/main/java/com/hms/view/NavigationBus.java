package com.hms.view;

import java.util.function.Consumer;

/**
 * A minimal pub/sub so a nested view (like a button inside DashboardView)
 * can ask the shell (MainLayoutController) to switch tabs, without the
 * nested controller needing a direct reference to the shell controller -
 * FXMLLoader creates each controller independently, so there's no natural
 * parent-child wiring between them otherwise.
 */
public final class NavigationBus {

    private static Consumer<String> handler;

    private NavigationBus() {
    }

    /** Called once by MainLayoutController when the shell is built. */
    public static void setHandler(Consumer<String> newHandler) {
        handler = newHandler;
    }

    /** Called by any nested controller to request a tab switch, e.g. "APPOINTMENTS". */
    public static void navigateTo(String route) {
        if (handler != null) {
            handler.accept(route);
        }
    }
}
