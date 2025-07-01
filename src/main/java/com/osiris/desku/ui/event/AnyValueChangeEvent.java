package com.osiris.desku.ui.event;


import com.google.gson.JsonObject;
import com.osiris.desku.Value;
import com.osiris.desku.ui.Component;

/**
 * Similar to {@link ValueChangeEvent} however the VALUE must not be linked to the component.
 * @param <COMP> any component
 * @param <VALUE> any value
 */
public class AnyValueChangeEvent<COMP extends Component<?,?>, VALUE> extends JavaScriptEvent<COMP> {
    public final VALUE value;
    public final VALUE valueBefore;

    /**
     * This constructor typically gets called from browser -> java. <br>
     * @param rawJSMessage expected in this format: <br>
     *                     {"newValue": "...", "eventAsJson": {...}} <br>
     *                     The provided value/json is EXPECTED to be already escaped (by the browser)!.
     * @param comp
     */
    public AnyValueChangeEvent(String rawJSMessage, COMP comp, VALUE valueBefore) {
        super(rawJSMessage, comp);
        this.valueBefore = valueBefore;
        this.value = (VALUE) Value.jsonElToVal(message.get("newValue"), comp);
    }

    /**
     * This constructor typically gets called from java -> browser. <br>
     * Prevent JSON->JAVA parsing of {@link #messageRaw} and {@link #value} by using this constructor instead.
     */
    public AnyValueChangeEvent(String rawJSMessage, JsonObject jsMessage, COMP comp, VALUE value, VALUE valueBefore, boolean isProgrammatic) {
        super(rawJSMessage, jsMessage, comp, isProgrammatic);
        this.value = value;
        this.valueBefore = valueBefore;
    }

    public String getValueAsString(){
        return message.get("newValue").getAsString();
    }
}
