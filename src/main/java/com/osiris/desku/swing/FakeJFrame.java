package com.osiris.desku.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.function.Consumer;

public class FakeJFrame extends JPanel {
    private BufferedImage lastRenderedImg;

    public void startRender(Consumer<BufferedImage> onRender, long sleepMs) {
        new Thread(() -> {
            try {
                while (true) {
                    render();
                    onRender.accept(lastRenderedImg);
                    Thread.sleep(sleepMs);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public Image render() {
        BufferedImage image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.createGraphics();
        this.paint(g);
        g.dispose();
        this.lastRenderedImg = image;
        return image;
    }

    public Image getLastRenderedImage() {
        return lastRenderedImg;
    }
}
