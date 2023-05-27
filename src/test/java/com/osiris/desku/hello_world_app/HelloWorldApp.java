package com.osiris.desku.hello_world_app;

import com.osiris.desku.*;

import static com.osiris.desku.Statics.*;
public class HelloWorldApp {
    public static void main(String[] args) throws Exception {
        // Setup app details
        App.init(new DesktopUIManager());
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return layout().add(text("Hello World!"));
        });

        // Create windows
        App.uis.create(home);
    }
}
