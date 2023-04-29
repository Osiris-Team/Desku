package com.osiris.desku;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.osiris.desku.swing.LoadingWindow;
import com.osiris.desku.swing.events.LoadStateChange;
import com.osiris.desku.ui.Theme;
import com.osiris.jlib.Stream;
import com.osiris.jlib.logger.AL;
import dev.lyze.flexbox.FlexBox;
import io.github.orioncraftmc.meditate.enums.YogaFlexDirection;
import io.github.orioncraftmc.meditate.enums.YogaWrap;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class App extends ApplicationAdapter {

    /**
     * Should be the directory in which this application was started. <br>
     * Can be used to store information that is not specific to an user. <br>
     */
    public static final File workingDir = new File(System.getProperty("user.dir"));
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
    public static CopyOnWriteArrayList<UI> windows = new CopyOnWriteArrayList<>();
    /**
     * The default theme that affects all views.
     */
    public static Theme theme = new Theme();

    public static CopyOnWriteArrayList<Runnable> onRender = new CopyOnWriteArrayList<>();
    public static Stage stage;
    public static FlexBox root;

    static {
        try {
            Logger.getGlobal().setLevel(Level.SEVERE);
            if (!AL.isStarted) {
                AL.start("Logger", true, new File(workingDir + "/latest.log"), false);
                AL.mirrorSystemStreams(new File(workingDir + "/mirror-out.log"), new File(workingDir + "/mirror-err.log"));
            }
            AL.info("Starting application...");
            AL.info("isOffscreenRendering = " + AppStartup.isOffscreenRendering);
            AL.info("workingDir = " + workingDir);
            AL.info("tempDir = " + tempDir);
            AL.info("userDir = " + userDir);
            // Create styles file
            styles.getParentFile().mkdirs();
            if (styles.exists()) styles.delete();
            styles.createNewFile();

            AL.info("Started application successfully!");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void create() {
        VisUI.load();
        stage = new Stage(new ScreenViewport());
        onRender.add(() -> {
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        });
        onRender.add(() -> {
            stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
            stage.draw();
        });
        root = new FlexBox();
        root.setFillParent(true);
        root.getRoot()
                .setFlexDirection(YogaFlexDirection.ROW)
                .setWrap(YogaWrap.WRAP);
        stage.addActor(root);

        Gdx.input.setInputProcessor(stage);

        stage.addListener(new InputListener() { // F12 to enable debug
            boolean debug = false;
            @Override
            public boolean keyDown (InputEvent event, int keycode) {
                if (keycode == Input.Keys.F12) {
                    debug = !debug;
                    root.setDebug(debug, true);
                    for (Actor actor : stage.getActors()) {
                        if (actor instanceof Group) {
                            Group group = (Group) actor;
                            group.setDebug(debug, true);
                        }
                    }
                    return true;
                }

                return false;
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        if (width == 0 && height == 0) return; //see https://github.com/libgdx/libgdx/issues/3673#issuecomment-177606278
        stage.getViewport().update(width, height, true);
        PopupMenu.removeEveryMenu(stage);
        class WindowResizeEvent extends Event {
        }
        WindowResizeEvent resizeEvent = new WindowResizeEvent();
        for (Actor actor : stage.getActors()) {
            actor.fire(resizeEvent);
        }
    }

    @Override
    public void render() {
        for (Runnable runnable : onRender) {
            runnable.run();
        }
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        VisUI.dispose();
        stage.dispose();
    }

    public static java.awt.Image getIcon() throws IOException {
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
    public static java.awt.Image getResourceImage(String path) throws IOException {
        String fullPath = (path.startsWith("/") ? path : "/" + path);
        return java.awt.Toolkit.getDefaultToolkit().getImage(getResourceURL(fullPath));
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
