package com.osiris.desku.simple_app.home;

import com.osiris.desku.App;
import com.osiris.desku.Route;
import com.osiris.desku.simple_app.about.About;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.osiris.desku.Statics.*;

public class Home extends Route {
    static {
        try {
            App.appendToGlobalCSS(App.getCSS(Home.class));
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
        Vertical ly = vertical().childGap(true).padding(true);
        ly.horizontalCL().size("100%", "70vh").childCenter()
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
        ly.add(text("Async").sizeXXL());
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
        ly.add(vertical().size("300px", "100px").laterWithOverlay((comp, overlay) -> {
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
        // Layouts and Text
        //
        ly.add(text("Simple layouts and text").sizeXXL());
        ly.verticalCL()
                .add(text("Vertical child layout. Items: "))
                .add(text("XSmall").sizeXS())
                .add(text("Small").sizeS())
                .add(text("Medium").sizeM())
                .add(text("Large").sizeL())
                .add(text("XLarge").sizeXL())
                .add(text("XLarge").sizeXL())
                .add(text("XXLarge").sizeXXL())
                .add(text("XXXLarge").sizeXXXL());

        ly.horizontalCL()
                .add(text("Horizontal child layout. Items: "))
                .add(text("XSmall").sizeXS())
                .add(text("Small").sizeS())
                .add(text("Medium").sizeM())
                .add(text("Large").sizeL())
                .add(text("XLarge").sizeXL())
                .add(text("XXLarge").sizeXXL())
                .add(text("XXXLarge").sizeXXXL());
        // Smart, mobile friendly layout
        ly.add(text("Smart, mobile friendly layout").sizeXXL());
        ly.add(smartlayout().add(
                text("1").putStyle("background-color", "var(--color-primary-50)"),
                text("2").putStyle("background-color", "var(--color-primary-50)"),
                text("3").putStyle("background-color", "var(--color-primary-50)"),
                text("4").putStyle("background-color", "var(--color-primary-50)"),
                text("5").putStyle("background-color", "var(--color-primary-50)")));
        // Scroll layout
        ly.add(text("Scroll layout").sizeXXL());
        ly.add(
                vertical().childGap(true).scrollable(true, "100%", "100px", // Size for scroll layout
                        "100%", "5px") // Min sizes for children
                        .onScroll(e -> {
                            AL.info("SCROLL: "+e.rawJSMessage);
                        })
        );
        for (int i = 0; i < 20; i++) {
            ly.lastChild().add(vertical().putStyle("background-color", "gray"));
        }
        // Page layout
        ly.add(text("Page layout").sizeXXL());
        int[] data = new int[10];
        for (int i = 0; i < 10; i++) {
            data[i] = i;
        }
        ly.add(
                pagelayout().childGap(true).setDataProvider(0, 3, (details) -> {
                    List<Component<?>> comps = new ArrayList<>();
                    for (int i = Math.max(details.iStart, 0); i < Math.min(details.iEnd, data.length - 1); i++) {
                        comps.add(text("Index: "+data[i]).width("100%").putStyle("background-color", "lightgray"));
                    }
                    try{Thread.sleep(1000);} catch (Exception e) {}
                    return comps;
                })
        );
        // Tab layout
        ly.add(text("Tab layout").sizeXXL());
        ly.add(tablayout().addTabAndPage("First", text("First page content"))
                .addTabAndPage("Second", text("Second page content"))
                .addTabAndPage("Third", text("Third page content")));

        //
        // Navigate between routes
        //
        ly.add(text("Navigate between routes").sizeXXL());
        ly.add(router().set(About.class).add(text("Go to About page!")));
        ly.add(router().set("/about").add(text("Go to About page!")));
        ly.add(router().set("https://google.com").add(text("Go to Google page!")));

        //
        // Overlays
        //
        ly.add(text("Overlays").sizeXXL());
        ly.add(overlay(null).add(text("Overlay over the page")));
        ly.verticalCL().putStyle("background-color", "blue").size("100px", "100px").add(
                overlay(ly.lastChild()).putStyle("background-color", "red")
                        .add(text("Overlay over another component.")));

        //
        // Inputs
        //
        ly.add(text("Inputs").sizeXXL());
        AtomicInteger i = new AtomicInteger();
        ly.horizontalCL().childGap(true).width("100%")
                .add(button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.text.set(s);
        }), button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.text.set(s);
        }), button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.text.set(s);
        }));
        // Button variants
        ly.horizontalCL().childGap(true).add(button("Primary").primary(), button("Secondary").secondary(), button("Success").success(),
                button("Danger").danger(), button("Warning").warning(), button("Info").info(),
                button("Light").light(), button("Dark").dark());
        // Fields
        ly.add(textfield("Text field label", "Def").onValueChange(e -> {
            AL.info("Input of textfield changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(passwordfield("Password field label").onValueChange(e -> {
            AL.info("Input of passwordfield changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(checkbox("Checkbox label").onValueChange(e -> {
            AL.info("Input of checkbox changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(colorpicker("Color picker label").onValueChange(e -> {
            AL.info("Input of colorpicker changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(slider("Slider label").onValueChange(e -> {
            AL.info("Input of slider changed: "+e.value+" before: "+e.valueBefore);
        }));
        // Selector
        ly.add(selector("Selector label").add(text("Option 1"), text("Option 2"), text("Option 3"))
                .onValueChange(e -> {
            AL.info("Input of selector changed: "+e.value+" before: "+e.valueBefore);
        }));
        // File chooser
        ly.add(filechooser("File chooser label").onValueChange(e -> {
            AL.info("Input of file chooser changed: "+e.value +" before: "+e.valueBefore);
            // File content can be accessed via e.content (byte array)
        }));

        //
        // Tables
        //
        ly.add(text("Tables").sizeXXL());
        ly.add(table().headers("Header 1", "Header 2")
                .row("Data 1", "Data 2aaaaa")
                .row("Data 3aa", "Data 4"));

        // Tables via reflection
        class Person {
            public final String firstName;
            public final String name;
            public final int age;

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
            ly.add(rtable(Person.class).rows(list)); // One liner ;)
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        //
        // Loading animations
        //
        ly.add(text("Loading animations").sizeXXL());
        ly.horizontalCL().childGap(true).add(spinner().primary(), spinner().secondary(), spinner().success(),
                spinner().danger(), spinner().warning(), spinner().info(),
                spinner().light(), spinner().dark());
        ly.horizontalCL().childGap(true).add(spinner().typeGrow().primary(), spinner().typeGrow().secondary(), spinner().typeGrow().success(),
                spinner().typeGrow().danger(), spinner().typeGrow().warning(), spinner().typeGrow().info(),
                spinner().typeGrow().light(), spinner().typeGrow().dark());

        return ly;
    }
}
