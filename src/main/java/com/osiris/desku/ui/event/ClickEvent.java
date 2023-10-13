package com.osiris.desku.ui.event;

import com.osiris.desku.ui.Component;

public class ClickEvent<T extends Component<?,?>> extends JavaScriptEvent<T> {
    public final boolean isTrusted;
    public final int screenX, screenY;

    public ClickEvent(String rawJSMessage, T comp) {
        super(rawJSMessage, comp);
        this.isTrusted = jsMessage.get("isTrusted").getAsBoolean();
        this.screenX = jsMessage.get("screenX").getAsInt();
        this.screenY = jsMessage.get("screenY").getAsInt();
    }
}
