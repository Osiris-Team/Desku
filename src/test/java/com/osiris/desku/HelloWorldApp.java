package com.osiris.desku;

import com.osiris.desku.swing.NativeWindow;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.Layout;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new Route("/"){
            @Override
            public Component<?> loadContent() {
                return new Layout().text("Currently at "+ path);
            }
        };

        // Create windows
        new NativeWindow(home);
    }
}
