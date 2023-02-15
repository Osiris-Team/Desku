package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;

public class Button extends Component<Button> {
    public Button(String txt) {
        init(this, "button");
        element.text(txt);
    }
}
