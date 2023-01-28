package com.osiris.desku.ui;

import com.osiris.desku.App;

public class HorizontalLayout extends Component<HorizontalLayout> {
    static {
        App.appendToGlobalStyles("" +
                "hl{" +
                "display: flex;" +
                "flex-direction: row;" +
                "}");
    }

    public HorizontalLayout() {
        init(this, "hl");
    }
}
