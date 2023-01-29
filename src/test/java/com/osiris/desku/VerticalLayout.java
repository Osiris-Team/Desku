package com.osiris.desku;

import com.osiris.desku.ui.Component;

/**
 * Example for a custom component.
 */
public class VerticalLayout extends Component<VerticalLayout> {

    static { // Executed only once
        App.appendToGlobalStyles("" +
                "vl{" + // Style affects all components with the tag "vl"
                "display: flex;" +
                "flex-direction: column;" +
                "}");
    }

    public VerticalLayout() {
        init(this, "vl");
        // Make sure to call init before anything else!
    }
}
