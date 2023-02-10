package com.osiris.desku.simple_app.home;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.*;

import java.io.IOException;

public class Home extends Route {
    static {
        try {
            App.appendToGlobalStyles(App.getCSS(Home.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Home() {
        super("/");
    }

    @Override
    public Component<?> loadContent() {
        Layout ly = new Layout();
        ly.horizontal().size("100%", "70vh").childCenter()
                .add(new Text("Build Desktop Apps with Java, HTML and CSS today!")
                        .sizeXXL().selfCenter()
                        .size("40vh", "20vh").bold())
                .add(new Image(this.getClass(), "/images/pc.png").width("30vw").selfCenter());
        ly.add(new Text("Click me!").selfCenter().onClick(event -> {
            System.out.println("Clicked text!");
            ly.add(new Text("Clicked text!"));
        }));
        ly.vertical().padding(true)
                .add(new Text("Child vertical layout. Items: "))
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL());
        ly.horizontal().padding(true)
                .add(new Text("Child horizontal layout. Items: "))
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL());

        // Overlays
        ly.add(new Overlay(null).add(new Text("Overlay over the page")));
        ly.vertical().stylePut("background-color", "blue").size("100px", "100px").add(
                new Overlay(ly.lastAdded).stylePut("background-color", "red")
                        .add(new Text("Overlay over parent.")));

        return ly;
    }
}
