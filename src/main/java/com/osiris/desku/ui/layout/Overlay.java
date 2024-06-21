package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

public class Overlay extends Component<Overlay, NoValue> {

    /**
     * @param parent if null, this overlay will be placed over the complete page,
     *               otherwise only over the provided parent component.
     */
    public Overlay(Component<?,?> parent) {
        super(NoValue.GET, "overlay");
        verticalCL();
        if (parent == null)
            this.sty("position", "fixed"); // Sit on top of page
        else {
            parent.sty("position", "relative"); // Sit on top of parent
            this.sty("position", "absolute");
        }
        sty("background-color", "rgba(0,0,0,0)"); // Transparent background
        sty("top", "0");
        sty("bottom", "0");
        sty("left", "0");
        sty("right", "0");
        sty("width", "fit-content");
        sty("height", "fit-content");
    }
}
