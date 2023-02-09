package com.osiris.desku.simple_app.about;

import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.Layout;
import com.osiris.desku.ui.Text;

public class About extends Route {
    public About() {
        super("/about");
    }
    @Override
    public Component<?> loadContent() {
        return new Layout().add(new Text("Currently at "+ path));
    }
}
