package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;

public class Input<VALUE> extends Component<Input<VALUE>, VALUE> {

    public Input(String type, VALUE defaultValue) {
        super(defaultValue, (Class<VALUE>) defaultValue.getClass(), "input");
        atr("slot", "input");
        atr("type", type);
        addClass("form-control");
        width("100%");
    }

    public Input(String type, VALUE defaultValue, String tag) {
        super(defaultValue, (Class<VALUE>) defaultValue.getClass(), tag);
        atr("slot", "input");
        atr("type", type);
        addClass("form-control");
        width("100%");
    }
}
