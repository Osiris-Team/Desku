package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.swing.Swing;
import com.osiris.desku.ui.utils.Rectangle;
import com.osiris.jlib.logger.AL;
import dev.webview.Webview;
import dev.webview.platform.OperatingSystem;
import dev.webview.platform.Platform;

import javax.swing.*;
import java.awt.Component;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Native window with HTML content that is generated by provided {@link Route}.
 */
public class DesktopUI extends UI {
    public JFrame frame;
    public Webview browser;
    public java.awt.Canvas browserContainer;

    public DesktopUI(Route route) throws Exception {
        this(route, false, true, 70, 60);
    }

    public DesktopUI(Route route, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        super(route, isTransparent, isDecorated, widthPercent, heightPercent);
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
     * @param isDecorated
     * @param widthPercent
     * @param heightPercent
     */
    public void init(String startURL, boolean isTransparent, boolean isDecorated, int widthPercent, int heightPercent) throws Exception {
        AtomicBoolean isBrowserReady = new AtomicBoolean(false);

        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Using createAWT allows you to defer the creation of the webview until the
        // canvas is fully renderable.
        browserContainer = (Canvas) Webview.createAWT(true, (browser) -> {
            this.browser = browser;

            browser.loadURL(startURL);
            try{
                Thread.sleep(10); // Otherwise below JS is not executed bc its to fast after loadURL() somehow?
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // Resize browser window too
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    Dimension contentSize = e.getComponent().getSize();
                    // Exclude decoration/top-bar from width/height
                    Insets insets = frame.getInsets();
                    int contentWidth = contentSize.width - insets.left - insets.right;
                    int contentHeight = contentSize.height - insets.top - insets.bottom;
                    // There is a random margin on Windows that isn't visible, so we must
                    // compensate. // TODO figure out why this is caused.
                    if (Platform.os == OperatingSystem.WINDOWS) {
                        contentWidth -= 16;
                        contentHeight -= 39;
                    }
                    int finalContentWidth = contentWidth;
                    int finalContentHeight = contentHeight;
                    browser.dispatch(() -> {
                        browser.setFixedSize(finalContentWidth, finalContentHeight);
                        //browserContainer.setSize(finalContentWidth, finalContentHeight); // This somehow breaks stuff
                    });
                }
            });

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    close();
                }
            });

            // Run the webview event loop, the webview is fully disposed when this returns.
            isBrowserReady.set(true);
            browser.run();
        });

        frame.getContentPane().add(browserContainer, BorderLayout.CENTER);
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

        // These must be called before showing the window
        decorate(isDecorated); // Must be called before background()
        if(isTransparent) background("#00000000");

        frame.setVisible(true);
        frame.requestFocus();

        while(!isBrowserReady.get()) Thread.yield();
    }

    @Override
    public void close() {
        browser.close();
        frame.dispose();
        super.close();
        if (App.uis.all.isEmpty()) System.exit(0);
    }

    /**
     * This invalidates the container and thus to see changes in the DesktopUI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public void width(int widthPercent) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            if (frame == null) onLoadStateChanged.addAction((action, isLoading) -> {
                if (!isLoading) {
                    action.remove();
                    updateWidth(frame.getParent(), frame, widthPercent);
                }
            }, AL::warn);
            else
                updateWidth(frame.getParent(), frame, widthPercent);
        });
    }

    /**
     * This invalidates the container and thus to see changes in the DesktopUI
     * make sure execute {@link java.awt.Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public void height(int heightPercent) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            if (frame == null) onLoadStateChanged.addAction((action, isLoading) -> {
                if (!isLoading) {
                    action.remove();
                    updateHeight(frame.getParent(), frame, heightPercent);
                }
            }, AL::warn);
            else
                updateHeight(frame.getParent(), frame, heightPercent);
        });
    }

    private void updateWidth(java.awt.Component parent, java.awt.Component target, int widthPercent) {
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

    private void updateHeight(java.awt.Component parent, Component target, int heightPercent) {
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

    public void plusX(int x) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setLocation(frame.getX() + x, frame.getY());
        });
    }

    public void plusY(int y) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setLocation(frame.getX(), frame.getY() + y);
        });
    }

    @Override
    public void executeJavaScript(String jsCode, String jsCodeSourceName, int jsCodeStartingLineNumber) {
        browser.dispatch(() -> {
            browser.eval(jsCode);
        });
    }

    @Override
    public void maximize(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setExtendedState(b ? JFrame.MAXIMIZED_BOTH : JFrame.NORMAL);
        });
    }

    @Override
    public void minimize(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setExtendedState(b ? JFrame.ICONIFIED : JFrame.NORMAL);
        });
    }

    @Override
    public void fullscreen(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices()[0];
            device.setFullScreenWindow(b ? frame : null);
        });
    }

    @Override
    public void onSizeChange(Consumer<com.osiris.desku.ui.utils.Rectangle> code) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    code.accept(new com.osiris.desku.ui.utils.Rectangle(
                            e.getComponent().getWidth(), e.getComponent().getHeight()));
                    frame.revalidate();
                }
            });
        });
    }

    @Override
    public Rectangle getScreenSize() throws InterruptedException, InvocationTargetException {
        AtomicReference<Rectangle> rec = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            java.awt.Rectangle screenSize = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);

            int width = screenSize.width - screenInsets.left - screenInsets.right;
            int height = screenSize.height - screenInsets.top - screenInsets.bottom;
            rec.set(new Rectangle(width, height));
        });
        return rec.get();
    }

    @Override
    public Rectangle getScreenSizeWithoutTaskBar() throws InterruptedException, InvocationTargetException {
        AtomicReference<Rectangle> rec = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = gd.getDefaultConfiguration();
            java.awt.Rectangle screenSize = gc.getBounds();
            Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            // TODO if window has decoration, add it to this calculation

            int width = screenSize.width - screenInsets.left - screenInsets.right;
            int height = screenSize.height - screenInsets.top - screenInsets.bottom;
            rec.set(new Rectangle(screenSize.width - width, screenSize.height - height));
        });
        return rec.get();
    }

    @Override
    public void decorate(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setUndecorated(!b);
        });
    }

    @Override
    public void allwaysOnTop(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            frame.setAlwaysOnTop(b);
        });
    }

    @Override
    public void focus(boolean b) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            if(b) frame.requestFocus();
            else frame.transferFocusBackward();
        });
    }

    public Color convertCSSColor(String cssHexColor) {
        if (cssHexColor.startsWith("#")) {
            // Remove the # symbol from the beginning
            cssHexColor = cssHexColor.substring(1);
        }

        int alpha = 255; // Default alpha value (fully opaque)
        if (cssHexColor.length() == 8) {
            // Extract the alpha value from the last two characters
            String alphaHex = cssHexColor.substring(6);
            alpha = Integer.parseInt(alphaHex, 16);
        }

        int red = Integer.parseInt(cssHexColor.substring(0, 2), 16);
        int green = Integer.parseInt(cssHexColor.substring(2, 4), 16);
        int blue = Integer.parseInt(cssHexColor.substring(4, 6), 16);

        return new Color(red, green, blue, alpha);
    }

    @Override
    public void background(String hexColor) throws InterruptedException, InvocationTargetException {
        SwingUtilities.invokeAndWait(() -> {
            Color color = convertCSSColor(hexColor);
            frame.setBackground(color);
            //frame.getContentPane().setBackground(color); TODO find out why this makes the frame clickthorugh
            //browserContainer.setBackground(color);
            if(content != null) content.sty("background-color", hexColor);

            if(hexColor.equals("#00000000")){
                //frame.setOpacity(0.99f); // Slightly transparent to allow mouse events
                //TODO doesnt fix shit
            }
        });
    }
}
