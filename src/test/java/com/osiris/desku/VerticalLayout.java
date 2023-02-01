package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.jlib.Stream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

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
            // You can also add a css file to the current classes' package/folder.
            // The file below is at "com/osiris/desku/VerticalLayout.css"
            //styles = Stream.toString(App.getResource(VerticalLayout.class.getName().replace(".","/") + ".css"));
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
