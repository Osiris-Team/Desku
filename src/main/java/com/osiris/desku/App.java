package com.osiris.desku;

import com.osiris.desku.swing.LoadingWindow;
import com.osiris.desku.swing.NativeWindow;
import com.osiris.jlib.logger.AL;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefMessageRouter;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    /**
     * Should be the directory in which this application was started. <br>
     * Can be used to store information that is not specific to an user. <br>
     */
    public static final File workingDir = new File(System.getProperty("user.dir"));
    public static final CefApp cef;
    public static final CefClient cefClient;
    public static String name = "My Todo";
    /**
     * Should get cleared by the operating system on reboots. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppData\Local\Temp\AppName
     */
    public static final File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + name);
    /**
     * Can be used to store user-specific data. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppName
     */
    public static final File userDir = new File(System.getProperty("user.home") + "/" + name);
    public static CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<NativeWindow> windows = new CopyOnWriteArrayList<>();
    public static int port;

    static {
        try {
            Logger.getGlobal().setLevel(Level.SEVERE);
            if (!AL.isStarted) {
                AL.start("Logger", true, new File(workingDir + "/latest.log"), false);
                AL.mirrorSystemStreams(new File(workingDir + "/mirror-out.log"), new File(workingDir + "/mirror-err.log"));
            }
            AL.info("Starting application...");
            AL.info("workingDir = " + workingDir);
            AL.info("tempDir = " + tempDir);
            AL.info("userDir = " + userDir);

            // (0) Initialize CEF using the maven loader
            CefAppBuilder builder = new CefAppBuilder();
            builder.setProgressHandler(new LoadingWindow().getProgressHandler());
            builder.getCefSettings().windowless_rendering_enabled = false;
            // USE builder.setAppHandler INSTEAD OF CefApp.addAppHandler!
            // Fixes compatibility issues with MacOSX
            builder.setAppHandler(new MavenCefAppHandlerAdapter() {
                @Override
                public void stateHasChanged(org.cef.CefApp.CefAppState state) {
                    // Shutdown the app if the native CEF part is terminated
                    if (state == CefApp.CefAppState.TERMINATED) System.exit(0);
                }
            });
            // (1) The entry point to JCEF is always the class CefApp. There is only one
            //     instance per application and therefore you have to call the method
            //     "getInstance()" instead of a CTOR.
            //
            //     CefApp is responsible for the global CEF context. It loads all
            //     required native libraries, initializes CEF accordingly, starts a
            //     background task to handle CEF's message loop and takes care of
            //     shutting down CEF after disposing it.
            //
            //     WHEN WORKING WITH MAVEN: Use the builder.build() method to
            //     build the CefApp on first run and fetch the instance on all consecutive
            //     runs. This method is thread-safe and will always return a valid app
            //     instance.
            cef = builder.build();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                CefApp.getInstance().dispose();
            }));
            // (2) JCEF can handle one to many browser instances simultaneous. These
            //     browser instances are logically grouped together by an instance of
            //     the class CefClient. In your application you can create one to many
            //     instances of CefClient with one to many CefBrowser instances per
            //     client. To get an instance of CefClient you have to use the method
            //     "createClient()" of your CefApp instance. Calling an CTOR of
            //     CefClient is not supported.
            //
            //     CefClient is a connector to all possible events which come from the
            //     CefBrowser instances. Those events could be simple things like the
            //     change of the browser title or more complex ones like context menu
            //     events. By assigning handlers to CefClient you can control the
            //     behavior of the browser. See tests.detailed.MainFrame for an example
            //     of how to use these handlers.
            cefClient = cef.createClient();
            // (3) Create a simple message router to receive messages from CEF.
            CefMessageRouter msgRouter = CefMessageRouter.create();
            cefClient.addMessageRouter(msgRouter);

            AL.info("Started application successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Image getIcon() throws IOException {
        try {
            return getResourceImage("/icon.png");
        } catch (Exception e) {
            AL.warn("Failed to load /icon.png from current jar! Set default icon.", e);
            return null; // TODO
        }
    }

    /**
     * @param path expected relative path to a file inside the current jar. Example: icon.png or /icon.png
     */
    public static Image getResourceImage(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        File img = new File(App.workingDir + fullPath);
        if (!img.exists()) {
            img.getParentFile().mkdirs();
            img.createNewFile();
            InputStream link = (App.class.getResourceAsStream(fullPath));
            Files.copy(link, img.toPath(), StandardCopyOption.REPLACE_EXISTING);
            link.close();
        }
        return Toolkit.getDefaultToolkit().getImage(img.getAbsolutePath());
    }

    /**
     * @param path expected relative path to a file inside the current jar. Example: help.txt or /help.txt
     */
    public static InputStream getResource(String path) {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        return App.class.getResourceAsStream(fullPath);
    }
}
