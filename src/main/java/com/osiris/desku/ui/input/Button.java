package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;

public class Button extends Component<Button> {
    public final Text text = new Text("Button");

    public Button(String txt) {
        init(this, "button");
        add(text);
        text.set(txt);
    }
}
