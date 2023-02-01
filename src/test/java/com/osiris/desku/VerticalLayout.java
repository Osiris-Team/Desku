package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.jlib.Stream;

/**
 * Example for a custom component.
 */
public class VerticalLayout extends Component<VerticalLayout> {

    static { // Executed only once
        try{
            String styles = "" +
                    "vl{" + // Style affects all components with the tag "vl"
                    "display: flex;" +
                    "flex-direction: column;" +
                    "}";
            // You can also add a css file the current classes' package/folder.
            // The file below is at "com/osiris/desku/VerticalLayout.css"
            //styles = Stream.toString(App.getResource(VerticalLayout.class.toString().replace(".","/") + ".css"));
            App.appendToGlobalStyles(styles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public VerticalLayout() {
        init(this, "vl");
        // Make sure to call init before anything else!
    }
}
