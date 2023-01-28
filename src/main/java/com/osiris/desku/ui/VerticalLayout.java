package com.osiris.desku.ui;

import com.osiris.desku.App;

public class VerticalLayout extends Component<VerticalLayout> {
    static {
        App.appendToGlobalStyles("" +
                "vl{" +
                "display: flex;" +
                "flex-direction: column;" +
                "}");
    }

    public VerticalLayout() {
        init(this, "vl");
    }
}
