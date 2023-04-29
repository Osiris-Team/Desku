package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;

public class Overlay extends Layout {

    /**
     * @param parent if null, page is used as parent.
     */
    public Overlay(Component<?> parent) {
        init(this, "overlay");
        if (parent == null)
            this.putStyle("position", "fixed"); // Sit on top of page
        else {
            parent.putStyle("position", "relative"); // Sit on top of parent
            this.putStyle("position", "absolute");
        }
        putStyle("background-color", "rgba(0,0,0,0)"); // Transparent background
        putStyle("top", "0");
        putStyle("bottom", "0");
        putStyle("left", "0");
        putStyle("right", "0");
        putStyle("width", "fit-content");
        putStyle("height", "fit-content");
    }
}
