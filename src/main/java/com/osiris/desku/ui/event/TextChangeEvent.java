package com.osiris.desku.ui.event;


public class TextChangeEvent<T> extends JavaScriptEvent<T> {
    public final String txt;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"changedText": "...", "eventAsJson": {...}}
     * @param comp
     */
    public TextChangeEvent(String rawJSMessage, T comp) {
        super(rawJSMessage, comp);
        this.txt = jsMessage.get("changedText").getAsString();
    }

}
