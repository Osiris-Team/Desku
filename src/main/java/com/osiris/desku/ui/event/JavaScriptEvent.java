package com.osiris.desku.ui.event;

import com.google.gson.JsonObject;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.json.JsonFile;

public abstract class JavaScriptEvent<T extends Component<?,?>> {
    /**
     * Raw event information that is passed over from JavaScript to Java.
     */
    public final transient String rawJSMessage;
    public final JsonObject jsMessage;
    /**
     * Component where this event originates from.
     */
    public final transient T comp;
    /**
     * True if change was caused programmatically and NOT by the user directly.
     */
    public boolean isProgrammatic = false;

    /**
     * @param rawJSMessage must be a clean and parseable JSON-Object. No additional cleaning will be done on the server-side.
     */
    public JavaScriptEvent(String rawJSMessage, T comp) {
        this.rawJSMessage = rawJSMessage;
        this.jsMessage = JsonFile.parser.fromJson(rawJSMessage, JsonObject.class);
        this.comp = comp;
    }

    public JavaScriptEvent(String rawJSMessage, JsonObject jsMessage, T comp) {
        this.rawJSMessage = rawJSMessage;
        this.jsMessage = jsMessage;
        this.comp = comp;
    }
}
