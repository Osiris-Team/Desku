package com.osiris.desku.ui;

public class Overlay extends Layout {

    /**
     * @param parent if null, page is used as parent.
     */
    public Overlay(Component<?> parent) {
        init(this, "overlay");
        if (parent == null)
            this.stylePut("position", "fixed"); // Sit on top of page
        else {
            parent.stylePut("position", "relative"); // Sit on top of parent
            this.stylePut("position", "absolute");
        }
        stylePut("background-color", "rgba(0,0,0,0)"); // Transparent background
        stylePut("top", "0");
        stylePut("bottom", "0");
        stylePut("left", "0");
        stylePut("right", "0");
        stylePut("width", "fit-content");
        stylePut("height", "fit-content");
    }
}
