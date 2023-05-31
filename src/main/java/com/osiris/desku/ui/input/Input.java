package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;

public class Input extends Component<Input> {

    public Input(String type) {
        super("input");
        putAttribute("slot", "input");
        putAttribute("type", type);
    }
}
