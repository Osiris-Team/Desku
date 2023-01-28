package com.osiris.desku.swing;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class Swing {
    public static void center(JFrame frame) {
        int screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width, screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        int width = frame.getWidth(), height = frame.getHeight();
        frame.setLocation((screenWidth / 2) - (width / 2), (screenHeight / 2) - (height / 2)); // Position frame in mid of screen
    }

    public static JFrame roundCorners(JFrame frame) {
        return roundCorners(frame, 20, 20);
    }

    public static JFrame roundCorners(JFrame frame, int arcw, int arch) {
        frame.setShape(new RoundRectangle2D.Double(0, 0, frame.getWidth(), frame.getHeight(), arcw, arch));
        return frame;
    }

    public static <T extends Component> T transparent(T comp) {
        comp.setBackground(new Color(0, 0, 0, 0));
        return comp;
    }

    public static JLabel image(Image image) {
        return new JLabel(new ImageIcon(image));
    }

    public static JLabel image(Image image, int width, int height) {
        return new JLabel(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_FAST)));
    }


}
