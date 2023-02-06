package com.osiris.desku;

import com.osiris.desku.swing.NativeWindow;
import com.osiris.desku.ui.Layout;
import com.osiris.desku.ui.Text;

import java.io.IOException;

public class OffscreenAppTest {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().add(new Text("Hello World!"));
        });

        // Create windows
        new NativeWindow(home, true, false, 70, 60);
    }
}
