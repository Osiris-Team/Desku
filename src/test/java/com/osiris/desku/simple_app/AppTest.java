package com.osiris.desku.simple_app;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.simple_app.about.About;
import com.osiris.desku.simple_app.home.Home;
import com.osiris.desku.UI;
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
        UI winHome = new UI(home);
        winHome.openDevTools();
        //new NativeWindow(about).plusX(20).plusY(20);


        // Exit main thread once all windows closed
        // Note that this is only required in this test
        // On your regular application it should be fine to exit the main thread
        while (!App.windows.isEmpty())
            Thread.sleep(1000);
    }
}