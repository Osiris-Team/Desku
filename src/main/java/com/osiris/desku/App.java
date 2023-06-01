package com.osiris.desku;

import com.osiris.desku.ui.Theme;
import com.osiris.jlib.Stream;
import com.osiris.jlib.logger.AL;
import org.apache.commons.io.FileUtils;
import org.jline.utils.OSUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
     * @see UI#jsAddPermanentCallback(String, Consumer, Consumer)
     */
    public static int webSocketServerPort = -1;
    /**
     * Port for the HTTP server which is used to provide HTML and its assets. <br>
     * If -1 port is determined automatically. <br>
     *
     * @see UI#jsAddPermanentCallback(String, Consumer, Consumer)
     */
    public static int httpServerPort = -1;

    /**
     * Should get cleared by the operating system on reboots. <br>
     * Example on Windows: <br>
     * C:\Users\UserName\AppData\Local\Temp\AppName
     */
    public static File tempDir = new File(System.getProperty("java.io.tmpdir") + "/" + name);
    public static File htmlDir = new File(workingDir + "/html");
    public static File styles = new File(htmlDir + "/global-styles.css");
    public static File javascript = new File(htmlDir + "/global-javascript.js");
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
            AL.info("workingDir = " + workingDir);
            AL.info("tempDir = " + tempDir);
            AL.info("userDir = " + userDir);
            AL.info("htmlDir = " + htmlDir);
            AL.info("Java = " + System.getProperty("java.vendor") + " " + System.getProperty("java.version"));

            // Clear the directory at each app startup, since
            // its aim is to provide a cache to load pages faster
            // while switching between pages or reloading pages at runtime.
            if (App.htmlDir.exists()) FileUtils.deleteDirectory(App.htmlDir);
            App.htmlDir.mkdirs();

            // Create styles file
            styles.getParentFile().mkdirs();
            if (styles.exists()) styles.delete();
            styles.createNewFile();

            // Create javascript file
            javascript.getParentFile().mkdirs();
            if (javascript.exists()) javascript.delete();
            javascript.createNewFile();

            // Append Bootstrap v5.3.0
            appendToGlobalCSS(getCSS(Bootstrap.class));
            appendToGlobalJS(getJS(Bootstrap.class));

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
        String p1 = App.workingDir + fullPath;
        File f = new File(p1);
        if (f.exists()) return Files.newInputStream(f.toPath());
        String p2 = App.workingDir + "/src/main/java" + fullPath;
        f = new File(p2);
        if (f.exists()) return Files.newInputStream(f.toPath());
        String p3 = App.workingDir + "/src/test/java" + fullPath;
        f = new File(p3); // Support JUnit tests
        if (f.exists()) return Files.newInputStream(f.toPath());
        String classpath = System.getProperty("java.class.path");
        Exception e = null;
        try {
            if (OSUtils.IS_WINDOWS) fullPath = fullPath.replace("/", "\\");
            String[] dirs = OSUtils.IS_WINDOWS ? classpath.split(";") : classpath.split(":");
            for (String _dir : dirs) {
                File dir = new File(_dir);
                Iterator<File> it = FileUtils.iterateFiles(dir, null, true);
                while (it.hasNext()) {
                    f = it.next();
                    if (f.isFile() && f.getAbsolutePath().endsWith(".jar")) {
                        try (ZipFile zipFile = new ZipFile(f)) {
                            Enumeration<? extends ZipEntry> entries = zipFile.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry entry = entries.nextElement();
                                if (!entry.isDirectory() && path.endsWith(entry.getName())) {
                                    return zipFile.getInputStream(entry);
                                }
                            }
                        }
                    } else {
                        if (f.isFile() && f.getAbsolutePath().endsWith(fullPath)) {
                            return Files.newInputStream(f.toPath());
                        }
                    }
                }
            }
        } catch (Exception e1) {
            e = e1;
        }
        AL.warn("Failed to find resource \"" + fullPath + "\", searched: " + p1 + " and " + p2 + " and " + p3 + " and class paths recursively: " + classpath + ".", e);
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
        String p1 = App.workingDir + fullPath;
        File f = new File(p1);
        if (f.exists()) return f.toURI().toURL();
        String p2 = App.workingDir + "/src/main/java" + fullPath;
        f = new File(p2);
        if (f.exists()) return f.toURI().toURL();
        String p3 = App.workingDir + "/src/test/java" + fullPath;
        f = new File(p3); // Support JUnit tests
        if (f.exists()) return f.toURI().toURL();
        String classpath = System.getProperty("java.class.path");
        Exception e = null;
        try {
            if (OSUtils.IS_WINDOWS) fullPath = fullPath.replace("/", "\\");
            String[] dirs = OSUtils.IS_WINDOWS ? classpath.split(";") : classpath.split(":");
            for (String _dir : dirs) {
                File dir = new File(_dir);
                Iterator<File> it = FileUtils.iterateFiles(dir, null, true);
                while (it.hasNext()) {
                    f = it.next();
                    if (f.isFile() && f.getAbsolutePath().endsWith(fullPath)) {
                        return f.toURI().toURL();
                    }
                }
            }
        } catch (Exception e1) {
            e = e1;
        }
        AL.warn("Failed to find resource \"" + fullPath + "\", searched: " + p1 + " and " + p2 + " and " + p3 + " and class paths (except jars) recursively: " + classpath + ".", e);
        return null;
    }

    /**
     * @param dir  {@link Class#getPackage()} to get the package/directory of a class.
     * @param path For example "/image.png" or "image.png" if that file is located in the provided package/directory.
     *             Also can be a file in a subdirectory "/dir/image.png" or "dir/image.png".
     */
    public static InputStream getResourceInPackage(Package dir, String path) throws IOException {
        if (!path.startsWith("/")) path = "/" + path;
        return App.getResource(dir.getName().replace(".", "/") + path);
    }

    /**
     * Append CSS to the global CSS stylesheet file that gets loaded for all {@link UI}s.
     *
     * @param s CSS code.
     */
    public static void appendToGlobalCSS(String s) {
        synchronized (styles) {
            try {
                Files.write(styles.toPath(), s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Append JavaScript (JS) to the global JS file that gets loaded for all {@link UI}s. <br>
     * The JS code gets executed once the HTML for the page finished loading.
     *
     * @param s JS code.
     */
    public static void appendToGlobalJS(String s) {
        synchronized (javascript) {
            try {
                Files.write(javascript.toPath(), s.getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
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
        return Stream.toString(App.getResourceInPackage(clazz.getPackage(), "/" + clazz.getSimpleName() + ".css"));
    }

    /**
     * Returns the .js file content as string.
     * The js file is expected to have the same name as the class and be in the same package.
     */
    public static String getJS(Class<?> clazz) throws IOException {
        return Stream.toString(App.getResourceInPackage(clazz.getPackage(), "/" + clazz.getSimpleName() + ".js"));
    }
}
