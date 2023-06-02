package com.osiris.desku.hello_world_app;

import com.osiris.desku.*;
import com.osiris.desku.ui.layout.Vertical;

import static com.osiris.desku.Statics.*;
public class HelloWorldApp {
    public static void main(String[] args) throws Exception {
        // Setup app details
        App.init(new DesktopUIManager());
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return vertical().add(text("Hello World!"));
        });

        // Create windows
        App.uis.create(home);
    }
}
