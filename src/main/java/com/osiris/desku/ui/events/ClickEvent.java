package com.osiris.desku.ui.events;

import com.osiris.desku.ui.Component;

public class ClickEvent extends JavaScriptEvent {
    public final boolean isTrusted;
    public final int screenX, screenY;

    public ClickEvent(String rawJSMessage, Component<?> comp) {
        super(rawJSMessage, comp);
        this.isTrusted = jsMessage.get("isTrusted").getAsBoolean();
        this.screenX = jsMessage.get("screenX").getAsInt();
        this.screenY = jsMessage.get("screenY").getAsInt();
    }
}
