package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;

public class Input<VALUE> extends Component<Input<VALUE>, VALUE> {

    public Input(String type, VALUE defaultValue) {
        super(defaultValue, (Class<VALUE>) defaultValue.getClass(), "input");
        putAttribute("slot", "input");
        putAttribute("type", type);
    }
}
