package com.osiris.desku.simple_app.about;

import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;

public class About extends Route {
    public About() {
        super("/about");
    }

    @Override
    public Component<?,?> loadContent() {
        return new Vertical().add(new Text("Currently at " + path));
    }
}
