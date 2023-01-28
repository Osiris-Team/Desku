package com.osiris.desku;

import com.osiris.desku.swing.NativeWindow;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import java.io.IOException;

public class HelloWorldApp {
    public static void main(String[] args) throws IOException {
        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new Route("/"){
            @Override
            public Node loadContent() {
                return new TextNode("Hello World!");
            }
        };

        // Create windows
        new NativeWindow(home);
    }
}
