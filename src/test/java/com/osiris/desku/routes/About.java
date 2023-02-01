package com.osiris.desku.routes;

import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.Layout;

public class About extends Route {
    public About() {
        super("/about");
    }
    @Override
    public Component<?> loadContent() {
        return new Layout().text("Currently at "+ path);
    }
}
