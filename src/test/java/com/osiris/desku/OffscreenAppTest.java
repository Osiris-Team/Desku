package com.osiris.desku;

import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Layout;
import com.osiris.jlib.logger.AL;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class OffscreenAppTest {
    @Test
    void test() throws Exception {
        //AppStartup.isOffscreenRendering = true;
        // The below somehow also triggers HeadlessExceptions even when using osr browser...
        //System.setProperty("java.awt.headless", "true");
        //System.out.println("Headless: " + GraphicsEnvironment.isHeadless());

        // Setup app details
        App.init(new DesktopUIManager(false));
        App.name = "My-App";

        // Create routes
        Route home = new MRoute("/", () -> {
            return new Layout().add(new Text("Hello World!"));
        });

        // Create windows
        DesktopUI win = (DesktopUI) App.uis.create(home, false, 70, 60);
        Thread.sleep(10000); // Returns directly when in osr mode, bc load event is broken
        BufferedImage img = win.browser.createScreenshot(true).get();
        File fimg = new File(App.workingDir + "/img.png");
        ImageIO.write(img, "PNG", new FileOutputStream(fimg));
        AL.info(fimg.toString());
    }
}
