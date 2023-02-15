package com.osiris.desku.ui.event;

import com.osiris.desku.ui.Component;

public class ClickEvent<T> extends JavaScriptEvent {
    public final boolean isTrusted;
    public final int screenX, screenY;

    public ClickEvent(String rawJSMessage, Component<T> comp) {
        super(rawJSMessage, comp);
        this.isTrusted = jsMessage.get("isTrusted").getAsBoolean();
        this.screenX = jsMessage.get("screenX").getAsInt();
        this.screenY = jsMessage.get("screenY").getAsInt();
    }
}
