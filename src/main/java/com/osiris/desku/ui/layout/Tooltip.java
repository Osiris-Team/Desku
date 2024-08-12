package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;

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

        UI ui = UI.get();
        ui.executeJavaScriptSafely(ui.jsGetComp("comp", parent.id) + "new bootstrap.Tooltip(comp)");
        return this;
    }
}
