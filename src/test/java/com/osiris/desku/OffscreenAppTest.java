package com.osiris.desku;

import com.osiris.desku.swing.NativeWindow;
import com.osiris.desku.ui.Layout;
import com.osiris.desku.ui.Text;
import com.osiris.jlib.logger.AL;
import org.junit.jupiter.api.Test;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class OffscreenAppTest {
    @Test
    void test() throws IOException, ExecutionException, InterruptedException {
        AppStartup.isOffscreenRendering = true;
        // The below somehow also triggers HeadlessExceptions even when using osr browser...
        //System.setProperty("java.awt.headless", "true");
        //System.out.println("Headless: " + GraphicsEnvironment.isHeadless());

        // Setup app details
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().add(new Text("Hello World!"));
        });

        // Create windows
        NativeWindow win = new NativeWindow(home, false, 70, 60);
        Thread.sleep(10000); // Returns directly when in osr mode, bc load event is broken
        BufferedImage img = win.browser.createScreenshot(true).get();
        File fimg = new File(App.workingDir + "/img.png");
        ImageIO.write(img, "PNG", new FileOutputStream(fimg));
        AL.info(fimg.toString());
    }
}
