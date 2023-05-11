package com.osiris.desku.hello_world_app;

import com.osiris.desku.App;
import com.osiris.desku.MRoute;
import com.osiris.desku.Route;
import com.osiris.desku.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Layout;

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
        new UI(home);
    }
}
