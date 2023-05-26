package com.osiris.desku.simple_app;

import com.osiris.desku.*;
import com.osiris.desku.simple_app.about.About;
import com.osiris.desku.simple_app.home.Home;

class AppTest {

    public static void main(String[] args) throws Exception {
        // Setup details
        App.init(new DesktopUIManager(false));
        App.name = "My-App";
        // before loading the page

        // Create routes
        Route home = new Home();
        Route about = new About();

        // Create windows
        DesktopUI winHome = (DesktopUI) App.uis.create(home);
        winHome.openDevTools();
        //new NativeWindow(about).plusX(20).plusY(20);

        // Exit main thread once all windows closed
        // Note that this is only required in this test
        // On your regular application it should be fine to exit the main thread
        while (!UIManager.all.isEmpty())
            Thread.sleep(1000);
    }
}