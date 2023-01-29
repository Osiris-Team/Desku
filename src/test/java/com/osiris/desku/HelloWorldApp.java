package com.osiris.desku;

import com.osiris.desku.swing.NativeWindow;
import com.osiris.desku.ui.Layout;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().text("Hello World!");
        });

        // Create windows
        new NativeWindow(home);
    }
}
