package com.osiris.desku.hello_world_app;

import com.osiris.desku.App;
import com.osiris.desku.MRoute;
import com.osiris.desku.Route;
import com.osiris.desku.swing.NativeWindow;
import com.osiris.desku.ui.Layout;
import com.osiris.desku.ui.Text;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().add(new Text("Hello World!"));
        });

        // Create windows
        new NativeWindow(home);
    }
}
