package com.osiris.desku;

import com.osiris.desku.swing.Swing;
import com.osiris.jlib.logger.AL;
import dev.webview.Webview;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Native window with HTML content that is generated by provided {@link Route}.
 */
public class DesktopUI extends UI {
    public JFrame frame;
    public Webview browser;
    public Component browserDesktopUI;

    public DesktopUI(Route route) throws Exception {
        this(route, false, 70, 60);
    }

    public DesktopUI(Route route, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        super(route, isTransparent, widthPercent, heightPercent);
    }

    /**
     * Initialises/Displays the window and loads the HTML from the provided startURL.
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its DesktopUI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser DesktopUI.
     *
     * @param startURL      URL of the HTML content. Example: http://localhost or https://google.com or file:///ABSOLUTE_PATH_TO_HTML_FILE
     * @param isTransparent
     * @param widthPercent
     * @param heightPercent
     */
    public void init(String startURL, boolean isTransparent, int widthPercent, int heightPercent) throws Exception {
        AtomicBoolean isLoaded = new AtomicBoolean(false);
        onLoadStateChanged.addAction((action, isLoading) -> {
            if (!isLoading) {
                action.remove();
                isLoaded.set(true);
            }
        }, Exception::printStackTrace);

        frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Using createAWT allows you to defer the creation of the webview until the
        // canvas is fully renderable.
        browserDesktopUI = Webview.createAWT(true, (wv) -> {
            browser = wv;

            wv.bind("tellJavaThatIsLoaded", e -> {
                onLoadStateChanged.execute(false); // stopped loading
                return null;
            });
            wv.loadURL(startURL);
            wv.eval("const event = new Event(\"pageloaded\");\n" +
                    "async function notifyOnPageLoad() {\n" +
                    "  setTimeout(function() {  \n" +
                    "    if (document.readyState === 'complete') {\n" +
                    "        console.log('Page finished loading.');\n" +
                    "        window.tellJavaThatIsLoaded().then(result => {});\n" +
                    "        document.dispatchEvent(event);\n" +
                    "    } else {\n" +
                    "      notifyOnPageLoad();\n" +
                    "    }\n" +
                    "  }, 100) // 100ms\n" +
                    "}\n" +
                    "console.log('Waiting for page to finish loading...')\n" +
                    "notifyOnPageLoad()\n");

            // Resize browser window too
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    wv.dispatch(() -> {
                        wv.setSize(e.getComponent().getWidth(), e.getComponent().getHeight());
                    });
                    frame.revalidate();
                }
            });

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });

            // Run the webview event loop, the webview is fully disposed when this returns.
            wv.run();
        });

        frame.getContentPane().add(browserDesktopUI, BorderLayout.CENTER);
        if (widthPercent <= 0 || heightPercent <= 0) {
            widthPercent = 100;
            heightPercent = 100;
        }
        width(widthPercent);
        height(heightPercent);
        frame.setIconImage(App.getIcon());
        frame.setTitle(App.name);
        Swing.center(frame);
        frame.revalidate();
        frame.setFocusable(true);
        frame.setVisible(true);
        frame.requestFocus();

        // JavaScript cannot be executed before the page is loaded
        while (!isLoaded.get()) Thread.yield();
    }

    @Override
    public void close() {
        browser.close();
        frame.dispose();
        super.close();
        if (UIManager.all.isEmpty()) System.exit(0);
    }

    /**
     * This invalidates the container and thus to see changes in the DesktopUI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public void width(int widthPercent) {
        if (frame == null) onLoadStateChanged.addAction((action, isLoading) -> {
            if (!isLoading) {
                action.remove();
                updateWidth(frame.getParent(), frame, widthPercent);
            }
        }, AL::warn);
        else
            updateWidth(frame.getParent(), frame, widthPercent);
    }

    /**
     * This invalidates the container and thus to see changes in the DesktopUI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public void height(int heightPercent) {
        if (frame == null) onLoadStateChanged.addAction((action, isLoading) -> {
            if (!isLoading) {
                action.remove();
                updateHeight(frame.getParent(), frame, heightPercent);
            }
        }, AL::warn);
        else
            updateHeight(frame.getParent(), frame, heightPercent);
    }

    private void updateWidth(Component parent, Component target, int widthPercent) {
        int parentWidth; // If no parent provided use the screen dimensions
        if (parent != null) parentWidth = parent.getWidth();
        else parentWidth =
                GraphicsEnvironment.isHeadless() ? 1920 // FHD
                        : Toolkit.getDefaultToolkit().getScreenSize().width;
        Dimension size = new Dimension(parentWidth / 100 * widthPercent, target.getHeight());
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    private void updateHeight(Component parent, Component target, int heightPercent) {
        int parentHeight; // If no parent provided use the screen dimensions
        if (parent != null) parentHeight = parent.getHeight();
        else parentHeight =
                GraphicsEnvironment.isHeadless() ? 1080 // FHD
                        : Toolkit.getDefaultToolkit().getScreenSize().height;
        Dimension size = new Dimension(target.getWidth(), parentHeight / 100 * heightPercent);
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    public void plusX(int x) {
        frame.setLocation(frame.getX() + x, frame.getY());
    }

    public void plusY(int y) {
        frame.setLocation(frame.getX(), frame.getY() + y);
    }

    @Override
    public void executeJavaScript(String jsCode, String jsCodeSourceName, int jsCodeStartingLineNumber) {
        browser.dispatch(() -> {
            browser.eval(jsCode);
        });
    }

    @Override
    public void maximize(boolean b) {
        frame.setExtendedState(b ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
    }

    @Override
    public void fullscreen(boolean b) {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
        device.setFullScreenWindow(b ? frame : null);
    }

    @Override
    public void onSizeChange(Consumer<SizeChange> code) {
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                code.accept(new SizeChange(e.getComponent().getWidth(), e.getComponent().getHeight()));
                frame.revalidate();
            }
        });
    }
}
