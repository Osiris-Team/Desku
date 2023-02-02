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
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
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
    public static final CefMessageRouter cefMessageRouter;
    public static final AtomicInteger cefMessageRouterRequestId = new AtomicInteger();
    public static String name = "My Todo";
    /**
     * Should get cleared by the operating system on reboots. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppData\Local\Temp\AppName
     */
    public static final File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + name);
    public static final File styles = new File(tempDir + "/global-styles.css");
    /**
     * Can be used to store user-specific data. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppName
     */
    public static final File userDir = new File(System.getProperty("user.home") + "/" + name);
    public static CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<NativeWindow> windows = new CopyOnWriteArrayList<>();

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
            // Create styles file
            styles.getParentFile().mkdirs();
            if (styles.exists()) styles.delete();
            styles.createNewFile();

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
            // (1) The entry point to JCEF is always the class CefApp.
            cef = builder.build();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                CefApp.getInstance().dispose();
            }));
            // (2) JCEF can handle one to many browser instances simultaneous.
            cefClient = cef.createClient();
            // (3) Create a simple message router to receive messages from CEF.
            CefMessageRouter.CefMessageRouterConfig config = new CefMessageRouter.CefMessageRouterConfig();
            config.jsQueryFunction = "cefQuery";
            config.jsCancelFunction = "cefQueryCancel";
            cefMessageRouter = CefMessageRouter.create(config);
            cefClient.addMessageRouter(cefMessageRouter);

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
        return Toolkit.getDefaultToolkit().getImage(getResourceURL(fullPath));
    }

    /**
     * If the provided resource cannot be found it also checks these directories: <br>
     * - App.workingDir <br>
     * - App.workingDir + "/src/main/java" <br>
     * - App.workingDir + "/src/test/java" <br>
     * @param path expected relative path to a file inside the current jar. Example: help.txt or /help.txt
     */
    public static InputStream getResource(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        InputStream in = App.class.getResourceAsStream(fullPath);
        if(in != null) return in;
        File f = new File(App.workingDir + fullPath);
        if(f.exists()) return Files.newInputStream(f.toPath());
        f = new File(App.workingDir + "/src/main/java" + fullPath);
        if(f.exists()) return Files.newInputStream(f.toPath());
        f = new File(App.workingDir + "/src/test/java" + fullPath); // Support JUnit tests
        if(f.exists()) return Files.newInputStream(f.toPath());
        return null;
    }

    /**
     * If the provided resource cannot be found it also checks these directories: <br>
     * - App.workingDir <br>
     * - App.workingDir + "/src/main/java" <br>
     * - App.workingDir + "/src/test/java" <br>
     * @param path expected relative path to a file inside the current jar. Example: help.txt or /help.txt
     */
    public static URL getResourceURL(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        URL url = App.class.getResource(fullPath);
        if(url != null) return url;
        File f = new File(App.workingDir + fullPath);
        if(f.exists()) return f.toURI().toURL();
        f = new File(App.workingDir + "/src/main/java" + fullPath);
        if(f.exists()) return f.toURI().toURL();
        f = new File(App.workingDir + "/src/test/java" + fullPath); // Support JUnit tests
        if(f.exists()) return f.toURI().toURL();
        return null;
    }

    public static void appendToGlobalStyles(String s) {
        synchronized (styles) {
            try {
                Files.write(styles.toPath(), s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
