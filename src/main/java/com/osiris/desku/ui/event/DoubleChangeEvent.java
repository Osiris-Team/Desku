package com.osiris.desku.ui.event;


import com.osiris.desku.ui.Component;

public class DoubleChangeEvent<T extends Component<?>> extends JavaScriptEvent<T> {
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
