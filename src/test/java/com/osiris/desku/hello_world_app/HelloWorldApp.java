package com.osiris.desku.hello_world_app;

import com.osiris.desku.*;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Layout;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException, UnsupportedPlatformException, CefInitializationException, InterruptedException {
        // Setup app details
        App.init(new DesktopUIManager(false));
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().add(new Text("Hello World!"));
        });

        // Create windows
        new DesktopUI(home);
    }
}
