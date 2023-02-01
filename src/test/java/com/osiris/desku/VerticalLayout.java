package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.jlib.Stream;

/**
 * Example for a custom component.
 */
public class VerticalLayout extends Component<VerticalLayout> {

    static { // Executed only once
        String styles = "" +
                "vl{" + // Style affects all components with the tag "vl"
                "display: flex;" +
                "flex-direction: column;" +
                "}";
        // You can also add a css file to your resources folder.
        // The file below will be named "com.osiris.desku.VerticalLayout.css"
        // styles = Stream.toString(App.getResource(VerticalLayout.class + ".css"));
        App.appendToGlobalStyles(styles);
    }

    public VerticalLayout() {
        init(this, "vl");
        // Make sure to call init before anything else!
    }
}
