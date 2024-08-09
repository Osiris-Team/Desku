package com.osiris.desku.ui.event;


import com.osiris.desku.ui.Component;

public class ScrollEvent<T extends Component<?,?>> extends JavaScriptEvent<T> {

    private final boolean isReachedEnd;
    private final double scrollHeight;
    private final double scrollTop;
    private final double clientHeight;

    /**
     * @param rawJSMessage expected in this format: <br>
     *                     {"isReachedEnd": "...", "scrollHeight": "...", "scrollTop": "...", "clientHeight": "...", "eventAsJson": {...}}
     * @param comp
     */
    public ScrollEvent(String rawJSMessage, T comp) {
        super(rawJSMessage, comp);
        this.isReachedEnd = message.get("isReachedEnd").getAsBoolean();
        this.scrollHeight = message.get("scrollHeight").getAsDouble();
        this.scrollTop = message.get("scrollTop").getAsDouble();
        this.clientHeight = message.get("clientHeight").getAsDouble();
    }

}
