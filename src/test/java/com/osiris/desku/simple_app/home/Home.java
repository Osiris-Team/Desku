package com.osiris.desku.simple_app.home;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.UI;
import com.osiris.desku.ui.*;
import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.layout.Layout;
import com.osiris.desku.ui.layout.Overlay;
import com.osiris.jlib.logger.AL;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

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
        ly.add(new Text("I am clickable text! Click me!").selfCenter().onClick(event -> {
            event.comp.set("Thank you for clicking!");
        }));

        //
        // Async
        //
        ly.add(new Text("Asynchronously update a component: Loading...").padding(true).async(txt -> {
            try{
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(1000);
                    txt.set("Asynchronously update a component: Loading... "+ i +"%");
                }
            } catch (Exception e) {
                AL.warn(e);
            }
        }));
        ly.add(new Layout().size("300px", "100px").asyncWithOverlay((comp, overlay) -> {
            try{
                Text txt = new Text("Waiting 10 seconds...");
                comp.add(txt);
                Thread.sleep(10000);
                txt.set("Finished after 10 seconds!");
                comp.putStyle("background", "#32a852");
            } catch (Exception e) {
                AL.warn(e);
            }
        }));

        //
        // Layouts
        //
        ly.vertical().padding(true)
                .add(new Text("Child vertical layout. Items: "))
                .add(new Text("XSmall").sizeXS())
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL())
                .add(new Text("XLarge").sizeXL())
                .add(new Text("XXLarge").sizeXXL())
                .add(new Text("XXXLarge").sizeXXXL());

        ly.horizontal().padding(true)
                .add(new Text("Child horizontal layout. Items: "))
                .add(new Text("XSmall").sizeXS())
                .add(new Text("Small").sizeS())
                .add(new Text("Medium").sizeM())
                .add(new Text("Large").sizeL())
                .add(new Text("XLarge").sizeXL())
                .add(new Text("XXLarge").sizeXXL())
                .add(new Text("XXXLarge").sizeXXXL());

        //
        // Overlays
        //
        ly.add(new Overlay(null).add(new Text("Overlay over the page")));
        ly.vertical().putStyle("background-color", "blue").size("100px", "100px").add(
                new Overlay(ly.lastAdded).putStyle("background-color", "red")
                        .add(new Text("Overlay over parent.")));

        //
        // Inputs
        //
        AtomicInteger i = new AtomicInteger();
        ly.add(new Button("This is a button!").onClick(e -> {
            String s = "Clicked "+i.incrementAndGet()+" times";
            System.out.println(s);
            e.comp.text.set(s);
        }));

        return ly;
    }
}
