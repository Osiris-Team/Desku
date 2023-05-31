package com.osiris.desku.ui.event;


public class DoubleChangeEvent<T> extends JavaScriptEvent<T> {
    public final double value;
    public final double valueBefore;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "eventAsJson": {...}}
     * @param comp
     */
    public DoubleChangeEvent(String rawJSMessage, T comp, double valueBefore) {
        super(rawJSMessage, comp);
        this.value = jsMessage.get("newValue").getAsDouble();
        this.valueBefore = valueBefore;
    }

}
