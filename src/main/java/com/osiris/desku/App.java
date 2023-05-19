package com.osiris.desku;

import com.osiris.desku.ui.Theme;
import com.osiris.jlib.Stream;
import com.osiris.jlib.logger.AL;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App {

    /**
     * Should be the directory in which this application was started. <br>
     * Can be used to store information that is not specific to an user. <br>
     */
    public static File workingDir = new File(System.getProperty("user.dir"));
    public static String name = "My Todo";
    /**
     * Examples: google.com or wikipedia.com or localhost
     */
    public static String domainName = "localhost";
    /**
     * Port for the WebSocket server which is used for fast communication between Java and JavaScript. <br>
     * If -1 port is determined automatically. <br>
     *
     * @see UI#addPermanentCallback(String, Consumer, Consumer)
     */
    public static int webSocketServerPort = -1;
    /**
     * Port for the HTTP server which is used to provide HTML and its assets. <br>
     * If -1 port is determined automatically. <br>
     *
     * @see UI#addPermanentCallback(String, Consumer, Consumer)
     */
    public static int httpServerPort = -1;

    /**
     * Should get cleared by the operating system on reboots. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppData\Local\Temp\AppName
     */
    public static File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + name);
    public static File styles = new File(tempDir + "/global-styles.css");
    /**
     * Can be used to store user-specific data. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppName
     */
    public static File userDir = new File(System.getProperty("user.home") + "/" + name);
    public static CopyOnWriteArrayList<Route> routes = new CopyOnWriteArrayList<>();
    /**
     * The default theme that affects all views.
     */
    public static Theme theme = new Theme();

    public static UIManager uis = null;

    public static void init(UIManager uiManager) {
        if (uiManager == null) {
            throw new NullPointerException("Provided UI factory is null!" +
                    " Make sure to provide an implementation for the platform this app is running in.");
        }
        App.uis = uiManager;
        try {
            Logger.getGlobal().setLevel(Level.SEVERE);
            if (!AL.isStarted) {
                AL.start("Logger", true, new File(workingDir + "/latest.log"), false);
                AL.mirrorSystemStreams(new File(workingDir + "/mirror-out.log"), new File(workingDir + "/mirror-err.log"));
            }
            AL.info("Starting application...");
            if (uiManager instanceof DesktopUIManager)
                AL.info("isOffscreenRendering = " + ((DesktopUIManager) uiManager).isOffscreenRendering);
            AL.info("workingDir = " + workingDir);
            AL.info("tempDir = " + tempDir);
            AL.info("userDir = " + userDir);
            AL.info("Java = " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));
            // Create styles file
            styles.getParentFile().mkdirs();
            if (styles.exists()) styles.delete();
            styles.createNewFile();

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
     *
     * @param path expected relative path to a file inside the current jar. Example: help.txt or /help.txt
     */
    public static InputStream getResource(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        InputStream in = App.class.getResourceAsStream(fullPath);
        if (in != null) return in;
        File f = new File(App.workingDir + fullPath);
        if (f.exists()) return Files.newInputStream(f.toPath());
        f = new File(App.workingDir + "/src/main/java" + fullPath);
        if (f.exists()) return Files.newInputStream(f.toPath());
        f = new File(App.workingDir + "/src/test/java" + fullPath); // Support JUnit tests
        if (f.exists()) return Files.newInputStream(f.toPath());
        return null;
    }

    /**
     * If the provided resource cannot be found it also checks these directories: <br>
     * - App.workingDir <br>
     * - App.workingDir + "/src/main/java" <br>
     * - App.workingDir + "/src/test/java" <br>
     *
     * @param path expected relative path to a file inside the current jar. Example: help.txt or /help.txt
     */
    public static URL getResourceURL(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        URL url = App.class.getResource(fullPath);
        if (url != null) return url;
        File f = new File(App.workingDir + fullPath);
        if (f.exists()) return f.toURI().toURL();
        f = new File(App.workingDir + "/src/main/java" + fullPath);
        if (f.exists()) return f.toURI().toURL();
        f = new File(App.workingDir + "/src/test/java" + fullPath); // Support JUnit tests
        if (f.exists()) return f.toURI().toURL();
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

    /**
     * Returns the .css file content as string.
     * The css file is expected to have the same name as the class and be in the same package.
     */
    public static String getCSS(Class<?> clazz) throws IOException {
        return Stream.toString(App.getResource(clazz.getName().replace(".", "/") + ".css"));
    }

    /**
     * Returns the .js file content as string.
     * The js file is expected to have the same name as the class and be in the same package.
     */
    public static String getJS(Class<?> clazz) throws IOException {
        return Stream.toString(App.getResource(clazz.getName().replace(".", "/") + ".js"));
    }

    /**
     * @param dir  {@link Class#getPackage()} to get the package/directory of a class.
     * @param path For example "image.png" if that file is located in the provided package/directory.
     */
    public static InputStream getResourceInPackage(Package dir, String path) throws IOException {
        return App.getResource(dir.getName().replace(".", "/") + path);
    }
}
