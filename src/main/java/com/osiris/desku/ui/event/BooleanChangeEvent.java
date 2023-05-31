package com.osiris.desku.ui.event;


public class BooleanChangeEvent<T> extends JavaScriptEvent<T> {
    public final boolean value;
    public final boolean valueBefore;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "eventAsJson": {...}}
     * @param comp
     */
    public BooleanChangeEvent(String rawJSMessage, T comp, boolean valueBefore) {
        super(rawJSMessage, comp);
        this.value = jsMessage.get("newValue").getAsBoolean();
        this.valueBefore = valueBefore;
    }

}
