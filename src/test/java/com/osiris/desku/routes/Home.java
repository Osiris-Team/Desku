package com.osiris.desku.routes;

import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.Layout;
import com.osiris.desku.ui.Text;

public class Home extends Route {
    public Home() {
        super("/");
    }

    @Override
    public Component<?> loadContent() {
        Layout ly = new Layout();
        ly.textM("Text M");
        ly.add(new Text("Text XL").selfEnd());
        return ly;
    }
}
