package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

public class Overlay extends Component<Overlay, NoValue> {
    public Vertical layout = new Vertical();

    /**
     * @param parent if null, this overlay will be placed over the complete page,
     *               otherwise only over the provided parent component.
     */
    public Overlay(Component<?,?> parent) {
        super(NoValue.GET, "overlay");
        add(layout);
        layout.sizeFull();
        _add = layout._add;
        _remove = layout._remove;
        if (parent == null)
            this.s("position", "fixed"); // Sit on top of page
        else {
            parent.s("position", "relative"); // Sit on top of parent
            this.s("position", "absolute");
        }
        s("background-color", "rgba(0,0,0,0)"); // Transparent background
        s("top", "0");
        s("bottom", "0");
        s("left", "0");
        s("right", "0");
        s("width", "fit-content");
        s("height", "fit-content");
    }
}
