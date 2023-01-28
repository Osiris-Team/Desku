package com.osiris.desku.swing;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Objects;

/**
 *
 */
public class NativeWindow extends JFrame {
    public final CefBrowser browser;
    public final Component browserUI;

    public NativeWindow(Route route) throws IOException {
        this("file:///" + route.writeToTempFile().getAbsolutePath());
    }

    public NativeWindow(String startURL) {
        this(startURL, false);
    }

    public NativeWindow(String startURL, boolean isTransparent) {
        this(startURL, isTransparent, 70, 60);
    }

    /**
     * To display a simple browser window, it suffices completely to create an
     * instance of the class CefBrowser and to assign its UI component to your
     * application (e.g. to your content pane).
     * But to be more verbose, this CTOR keeps an instance of each object on the
     * way to the browser UI.
     */
    public NativeWindow(String startURL, boolean isTransparent, int widthPercent, int heightPercent) {
        try {
            App.windows.add(this);
            // (4) One CefBrowser instance is responsible to control what you'll see on
            //     the UI component of the instance. It can be displayed off-screen
            //     rendered or windowed rendered. To get an instance of CefBrowser you
            //     have to call the method "createBrowser()" of your CefClient
            //     instances.
            //
            //     CefBrowser has methods like "goBack()", "goForward()", "loadURL()",
            //     and many more which are used to control the behavior of the displayed
            //     content. The UI is held within a UI-Compontent which can be accessed
            //     by calling the method "getUIComponent()" on the instance of CefBrowser.
            //     The UI component is inherited from a java.awt.Component and therefore
            //     it can be embedded into any AWT UI.
            browser = App.cefClient.createBrowser(startURL, false, isTransparent);
            browserUI = browser.getUIComponent();

            // (6) All UI components are assigned to the default content pane of this
            //     JFrame and afterwards the frame is made visible to the user.
            getContentPane().add(browserUI, BorderLayout.CENTER);
            if (widthPercent <= 0 || heightPercent <= 0) {
                widthPercent = 100;
                heightPercent = 100;
            }
            width(widthPercent);
            height(heightPercent);
            setIconImage(App.getIcon());
            setTitle(App.name);
            Swing.center(this);
            revalidate();
            setVisible(true);

            // (7) To take care of shutting down CEF accordingly, it's important to call
            //     the method "dispose()" of the CefApp instance if the Java
            //     application will be closed. Otherwise you'll get asserts from CEF.
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    App.windows.remove(this);
                    dispose();
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        browser.doClose();
    }

    /**
     * @see #width(int)
     */
    public void widthFull() {
        width(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param widthPercent 0 to 100% of the parent size (screen if null).
     */
    public void width(int widthPercent) {
        Objects.requireNonNull(this);
        updateWidth(this.getParent(), this, widthPercent);
    }

    /**
     * @see #height(int)
     */
    public void heightFull() {
        height(100);
    }

    /**
     * This invalidates the container and thus to see changes in the UI
     * make sure execute {@link Component#revalidate()} manually.
     *
     * @param heightPercent 0 to 100% of the parent size (screen if null).
     */
    public void height(int heightPercent) {
        updateHeight(this.getParent(), this, heightPercent);
    }

    private void updateWidth(Component parent, Component target, int widthPercent) {
        int parentWidth; // If no parent provided use the screen dimensions
        if (parent != null) parentWidth = parent.getWidth();
        else parentWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        Dimension size = new Dimension(parentWidth / 100 * widthPercent, target.getHeight());
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    private void updateHeight(Component parent, Component target, int heightPercent) {
        int parentHeight; // If no parent provided use the screen dimensions
        if (parent != null) parentHeight = parent.getHeight();
        else parentHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        Dimension size = new Dimension(target.getWidth(), parentHeight / 100 * heightPercent);
        target.setSize(size);
        target.setPreferredSize(size);
        target.setMaximumSize(size);
    }

    public NativeWindow plusX(int x) {
        this.setLocation(getX() + x, getY());
        return this;
    }

    public NativeWindow plusY(int y) {
        this.setLocation(getX(), getY() + y);
        return this;
    }
}
