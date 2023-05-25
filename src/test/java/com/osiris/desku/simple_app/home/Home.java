package com.osiris.desku.simple_app.home;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.display.RTable;
import com.osiris.desku.ui.display.Table;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.layout.Layout;
import com.osiris.desku.ui.layout.Overlay;
import com.osiris.jlib.logger.AL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import static com.osiris.desku.Statics.*;

public class Home extends Route {
    static {
        try {
            App.appendToGlobalStyles(App.getCSS(Home.class));
            App.appendToGlobalJS(App.getJS(Home.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Home() {
        super("/");
    }

    @Override
    public Component<?> loadContent() {
        Layout ly = layout().spacing(true).padding(true);
        ly.horizontal().size("100%", "70vh").childCenter()
                .add(text("Build Desktop Apps with Java, HTML and CSS today!")
                        .sizeXXL().selfCenter()
                        .size("40vh", "20vh").bold())
                .add(image(this.getClass(), "/images/pc.png").width("30vw").selfCenter());
        ly.add(text("I am clickable text! Click me!").selfCenter().onClick(event -> {
            event.comp.set("Thank you for clicking!");
        }));

        //
        // Async
        //
        ly.add(text("Asynchronously update a component: Loading...").padding(true).later(txt -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(1000);
                    txt.set("Asynchronously update a component: Loading... " + i + "%");
                }
            } catch (Exception e) {
                AL.warn(e);
            }
        }));
        ly.add(layout().size("300px", "100px").laterWithOverlay((comp, overlay) -> {
            try {
                Text txt = text("Waiting 10 seconds...");
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
        ly.vertical()
                .add(text("Child vertical layout. Items: "))
                .add(text("XSmall").sizeXS())
                .add(text("Small").sizeS())
                .add(text("Medium").sizeM())
                .add(text("Large").sizeL())
                .add(text("XLarge").sizeXL())
                .add(text("XLarge").sizeXL())
                .add(text("XXLarge").sizeXXL())
                .add(text("XXXLarge").sizeXXXL());

        ly.horizontal()
                .add(text("Child horizontal layout. Items: "))
                .add(text("XSmall").sizeXS())
                .add(text("Small").sizeS())
                .add(text("Medium").sizeM())
                .add(text("Large").sizeL())
                .add(text("XLarge").sizeXL())
                .add(text("XXLarge").sizeXXL())
                .add(text("XXXLarge").sizeXXXL());

        //
        // Overlays
        //
        ly.add(overlay(null).add(text("Overlay over the page")));
        ly.vertical().putStyle("background-color", "blue").size("100px", "100px").add(
                overlay(ly.lastAdded).putStyle("background-color", "red")
                        .add(text("Overlay over parent.")));

        //
        // Inputs
        //
        AtomicInteger i = new AtomicInteger();
        ly.horizontal().spacing(true).width("100%")
                .add(button("This is a button!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            System.out.println(s);
            e.comp.text.set(s);
        }), button("This is a button!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            System.out.println(s);
            e.comp.text.set(s);
        }), button("This is a button!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            System.out.println(s);
            e.comp.text.set(s);
        }));

        //
        // Tables
        //
        ly.add(new Table().headers("Header 1", "Header 2")
                .row("Data 1", "Data 2aaaaa")
                .row("Data 3aa", "Data 4"));

        // Tables via reflection
        class Person {
            public String firstName;
            public String name;
            public int age;

            public Person(String firstName, String name, int age) {
                this.firstName = firstName;
                this.name = name;
                this.age = age;
            }
        }
        List<Person> list = new ArrayList<>();
        list.add(new Person("John", "Stamos", 34));
        list.add(new Person("Peter", "Rigid", 56));
        list.add(new Person("Mariaaaaaa", "Francois", 33));
        try {
            ly.add(new RTable(Person.class).rows(list)); // One liner ;)
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }


        return ly;
    }
}
