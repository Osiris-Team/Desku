package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;

public class Input<VALUE> extends Component<Input<VALUE>, VALUE> {

    public Input(String type, VALUE defaultValue, Class<VALUE> defaultValueClass) {
        super(defaultValue, defaultValueClass, "input");
        atr("slot", "input");
        atr("type", type);
        addClass("form-control");
        grow(1);
        width("unset"); // Fix weird bootstrap styling
    }

    public Input(String type, VALUE defaultValue, Class<VALUE> defaultValueClass, String tag) {
        super(defaultValue, defaultValueClass, tag);
        atr("slot", "input");
        atr("type", type);
        addClass("form-control");
        grow(1);
        width("unset"); // Fix weird bootstrap styling
    }
}
