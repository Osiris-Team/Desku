package com.osiris.desku.ui.event;


import com.osiris.desku.ui.Component;

public class BooleanChangeEvent<T extends Component<?>> extends JavaScriptEvent<T> {
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
