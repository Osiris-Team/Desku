package com.osiris.desku.ui.event;

import com.google.gson.JsonObject;
import com.osiris.jlib.json.JsonFile;

public abstract class JavaScriptEvent<T> {
    /**
     * Raw event information that is passed over from JavaScript to Java.
     */
    public final transient String rawJSMessage;
    public final JsonObject jsMessage;
    /**
     * Component where this event originates from.
     */
    public final transient T comp;

    public JavaScriptEvent(String rawJSMessage, T comp) {
        this.rawJSMessage = rawJSMessage;
        this.comp = comp;
        this.jsMessage = JsonFile.parser.fromJson(rawJSMessage, JsonObject.class);
    }

    public JsonObject toJson() {
        return JsonFile.parser.fromJson(toJsonString(), JsonObject.class);
    }

    public String toJsonString() {
        return JsonFile.parser.toJson(this);
    }
}
