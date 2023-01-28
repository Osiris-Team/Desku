package com.osiris.desku.swing;

import com.osiris.desku.App;
import com.osiris.jlib.logger.AL;
import me.friwi.jcefmaven.EnumProgress;
import me.friwi.jcefmaven.IProgressHandler;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class LoadingWindow extends JFrame {
    JLabel txtStatus = Swing.transparent(new JLabel("Loading..."));

    public LoadingWindow() throws HeadlessException, IOException {
        super(App.name);
        setIconImage(App.getIcon());
        setVisible(false);
        setUndecorated(true);
        setBackground(Color.white);

        Image icon = App.getIcon();
        JPanel title = Swing.transparent(new JPanel());
        getContentPane().add(title);
        title.add(Swing.transparent(Swing.image(icon, 30, 30)));
        JLabel appName = new JLabel(App.name);
        title.add(appName);
        title.add(txtStatus);
        appName.setForeground(Color.GRAY);
        getContentPane().add(txtStatus);
        txtStatus.setForeground(Color.GRAY);

        setSize(300, 100);
        Swing.center(this);
        Swing.roundCorners(this, 10, 10);
    }

    public IProgressHandler getProgressHandler() {
        return (state, percent) -> {
            if (state == EnumProgress.INITIALIZED) {
                close();
            } else {
                this.setVisible(true);
            }
            Objects.requireNonNull(state, "state cannot be null");
            if (percent == -1.0F || !(percent < 0.0F) && !(percent > 100.0F)) {
                AL.info("JCEF " + state + " |> " + (percent == -1.0F ? "" : (int) percent + "%")); // cast to int, since It's anyways always .0
            } else {
                throw new RuntimeException("percent has to be -1f or between 0f and 100f. Got " + percent + " instead");
            }
            if (this.isVisible()) {
                txtStatus.setText("JCEF dependency: " + state.name().toLowerCase() + "... " + (percent == -1.0F ? "" : (int) percent + "%"));
                //matchContentSize();
            }
        };
    }

    public void close() {
        setVisible(false);
        dispose();
    }
}
