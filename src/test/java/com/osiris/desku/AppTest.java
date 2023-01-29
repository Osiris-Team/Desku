package com.osiris.desku;

import com.osiris.desku.routes.About;
import com.osiris.desku.routes.Home;
import com.osiris.desku.swing.NativeWindow;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class AppTest {
    @Test
    void main() throws IOException, InterruptedException {
        // Setup details
        App.name = "My-App";
        // before loading the page

        // Create routes
        Route home = new Home();
        Route about = new About();

        // Create windows
        NativeWindow winHome = new NativeWindow(home);
        winHome.openDevTools();
        //new NativeWindow(about).plusX(20).plusY(20);


        // Exit main thread once all windows closed
        // Note that this is only required in this test
        // On your regular application it should be fine to exit the main thread
        while (!App.windows.isEmpty())
            Thread.sleep(1000);
    }
}