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
        ly.add(new Text("Click me!").selfCenter().onClick(event -> {
            System.out.println("Clicked text!");
            ly.add(new Text("Clicked text!"));
        }));
        ly.vertical()
                .add(new Text("Child vertical layout. Items: "))
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL());
        ly.horizontal()
                .add(new Text("Child horizontal layout. Items: "))
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL());
        return ly;
    }
}
