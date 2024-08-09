package com.osiris.desku.ui.event;

import com.google.gson.JsonObject;
import com.osiris.desku.Value;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.json.JsonFile;

public abstract class JavaScriptEvent<T extends Component<?,?>> {
    /**
     * Raw event information that is passed over from JavaScript to Java.
     */
    public final transient String messageRaw;
    public final JsonObject message;
    /**
     * Component where this event originates from.
     */
    public final transient T comp;
    /**
     * True if this event was triggered programmatically and NOT by the user directly.
     */
    public boolean isProgrammatic = false;

    /**
     * @param messageRaw message must be a clean and parseable JSON-Object. No additional cleaning will be done on the server-side.
     */
    public JavaScriptEvent(String messageRaw, T comp) {
        this.messageRaw = Value.unescapeForJavaScript(messageRaw);
        this.message = JsonFile.parser.fromJson(this.messageRaw, JsonObject.class);
        this.comp = comp;
    }

    public JavaScriptEvent(String messageRaw, JsonObject message, T comp, boolean isProgrammatic) {
        this.messageRaw = messageRaw;
        this.message = message;
        this.comp = comp;
        this.isProgrammatic = isProgrammatic;
    }
}
