package com.osiris.desku.simple_app;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.simple_app.about.About;
import com.osiris.desku.simple_app.home.Home;
import com.osiris.desku.ui.DesktopUI;
import com.osiris.desku.ui.DesktopUIManager;

class AppTest {

    public static void main(String[] args) throws Exception {
        // Setup details before init
        App.uis = new DesktopUIManager();
        App.name = "My-Example-Desku-App";
        App.init();

        // Create routes
        Route home = new Home();
        Route about = new About();

        // Create windows
        DesktopUI winHome = (DesktopUI) App.uis.create(home);
        //DesktopUI winHomeTransparent = (DesktopUI) App.uis.create(home, true, false, 30, 50);
        //new NativeWindow(about).plusX(20).plusY(20);

        // Exit main thread once all windows closed
        // Note that this is only required in this test
        // On your regular application it should be fine to exit the main thread
        while (!App.uis.all.isEmpty())
            Thread.sleep(1000);
    }
}