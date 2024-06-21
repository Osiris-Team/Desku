package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;

public class Tooltip{
    public Component<?, ?> parent;
    public String content;

    public Tooltip(Component<?, ?> parent, String content) {
        this.parent = parent;
        this.content = content;
    }

    public Tooltip attachToParent(){
        parent.atr("data-bs-toggle", "tooltip");
        parent.atr("data-bs-title", content);

        parent.executeJS("new bootstrap.Tooltip(comp)");
        return this;
    }
}
