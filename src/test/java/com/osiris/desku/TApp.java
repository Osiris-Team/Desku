package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.DesktopUIManager;
import com.osiris.desku.ui.UI;

import java.util.function.Supplier;

/**
 * Test App used globally for all tests.
 */
public class TApp {
    public static MRoute route;
    public static UI ui;

    public static void load(Supplier<Component<?,?>> onLoad){
        App.init(new DesktopUIManager());
        route = new MRoute("/", onLoad);
        try {
            ui = App.uis.create(route);
            while(ui.isLoading()) Thread.yield();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
