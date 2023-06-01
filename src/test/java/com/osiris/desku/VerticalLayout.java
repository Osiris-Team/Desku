package com.osiris.desku;

import com.osiris.desku.ui.Component;

/**
 * Example for a custom component.
 */
public class VerticalLayout extends Component<VerticalLayout> {

    static { // Executed only once
        try {
            String styles = "" +
                    "vl{" + // Style affects all components with the tag "vl"
                    "display: flex;" +
                    "flex-direction: column;" +
                    "}";
            // You can also add a css file to the current classes' package/folder.
            // The file below is at "com/osiris/desku/VerticalLayout.css"
            //styles = App.getCSS(VerticalLayout.class);
            App.appendToGlobalCSS(styles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VerticalLayout() {
        super("vl");
        // Make sure to call init before anything else!

        // You can get the window this component is loaded in, like so:
        System.out.println(UI.get());
    }
}
