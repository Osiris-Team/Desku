package com.osiris.desku.swing;

import com.osiris.desku.App;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

@SuppressWarnings("serial")
public class DevToolsDialog extends JFrame {
    private final CefBrowser devTools_;

    public DevToolsDialog(String title, CefBrowser browser) {
        this(title, browser, null);
    }

    public DevToolsDialog(String title, CefBrowser browser, Point inspectAt) {
        try {
            setIconImage(App.getIcon());
        } catch (IOException e) {
            e.printStackTrace();
        }
        setTitle(title);
        setLayout(new BorderLayout());
        setSize(800, 600);
        setLocation(this.getLocation().x + 20, this.getLocation().y + 20);

        devTools_ = browser.getDevTools(inspectAt);
        add(devTools_.getUIComponent());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                dispose();
            }
        });

        setVisible(true);
    }

    @Override
    public void dispose() {
        devTools_.close(true);
        super.dispose();
    }
}