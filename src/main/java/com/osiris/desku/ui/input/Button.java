package com.osiris.desku.ui.input;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;

public class Button extends Component<Button> {

    static {
        try {
            App.appendToGlobalStyles(App.getCSS(Button.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final Text text = new Text("Button").bold().putStyle("color", "var(--color-contrast)");

    public Button(String txt) {
        super("button");
        add(text);
        text.set(txt);
        element.addClass("btn");
    }
}
