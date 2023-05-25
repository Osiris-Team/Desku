package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;

public class Overlay extends Component<Overlay> {
    public Layout layout = new Layout();

    /**
     * @param parent if null, this overlay will be placed over the complete page,
     *               otherwise only over the provided parent component.
     */
    public Overlay(Component<?> parent) {
        super("overlay");
        add(layout);
        layout.sizeFull();
        _add = layout._add;
        _remove = layout._remove;
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
