package com.osiris.desku.ui.event;


public class TextChangeEvent<T> extends JavaScriptEvent<T> {
    public final String value;
    public final String valueBefore;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "eventAsJson": {...}}
     * @param comp
     */
    public TextChangeEvent(String rawJSMessage, T comp, String valueBefore) {
        super(rawJSMessage, comp);
        this.value = jsMessage.get("newValue").getAsString();
        this.valueBefore = valueBefore;
    }

}
