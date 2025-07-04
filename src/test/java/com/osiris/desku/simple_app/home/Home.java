package com.osiris.desku.simple_app.home;

import com.osiris.desku.App;
import com.osiris.desku.Icon;
import com.osiris.desku.Route;
import com.osiris.desku.simple_app.about.About;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.input.TextField;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.Popup;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.jlib.logger.AL;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
    public Component<?,?> loadContent() {
        Vertical ly = vertical().childGap(true).padding(true);

        //
        // Images
        //
        // Existing image in the same folder as this class
        ly.add(
                image(this.getClass(), "/images/desku_banner.png").width("100%")
        );
        // In-memory created 1x1 black image
        ly.add(
                image(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "black.png")
                        .width("100%").height("10vh")
        );

        //
        // Events
        //
        ly.add(text("Build Desktop Apps with Java/JS, HTML and CSS today! I am clickable text! Click me!").selfCenter2().onClick(event -> {
            event.comp.setValue("Thank you for clicking!");
        }));

        //
        // Async
        //
        ly.add(text("Async").sizeXXL());
        ly.add(text("Asynchronously update a component: Loading...").padding(true).later(txt -> {
            try {
                for (int i = 1; i <= 100; i++) {
                    Thread.sleep(1000);
                    txt.setValue("Asynchronously update a component: Loading... " + i + "%");
                }
                txt.setValue("Loaded fully!");
            } catch (Exception e) {
                AL.warn(e);
            }
        }));
        ly.add(vertical().size("300px", "100px").laterWithOverlay((comp, overlay) -> {
            try {
                Text txt = text("Waiting 10 seconds...");
                comp.add(txt);
                Thread.sleep(10000);
                txt.setValue("Finished after 10 seconds!");
                comp.sty("background", "#32a852");
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
                .add(text("XXXLarge").sizeXXXL())
                .add(text(null));

        ly.horizontalCL()
                .add(text("Horizontal child layout. Items: "))
                .add(text("XSmall").sizeXS())
                .add(text("Small").sizeS())
                .add(text("Medium").sizeM())
                .add(text("Large").sizeL())
                .add(text("XLarge").sizeXL())
                .add(text("XXLarge").sizeXXL())
                .add(text("XXXLarge").sizeXXXL())
                .add(text(null));
        // Smart, mobile friendly layout
        ly.add(text("Smart, mobile friendly layout").sizeXXL());
        ly.add(smartlayout().add(
                text("1").sty("background-color", "var(--color-primary-50)"),
                text("2").sty("background-color", "var(--color-primary-50)"),
                text("3").sty("background-color", "var(--color-primary-50)"),
                text("4").sty("background-color", "var(--color-primary-50)"),
                text("5").sty("background-color", "var(--color-primary-50)")));
        // Scroll layout
        ly.add(text("Scroll layout").sizeXXL());
        ly.add(
                vertical().childGap(true).scrollable(true, "100%", "100px", // Size for scroll layout
                        "100%", "5px") // Min sizes for children
                        .onScroll(e -> {
                            AL.info("SCROLL: "+e.messageRaw);
                        })
        );
        for (int i = 0; i < 20; i++) {
            ly.lastChild().add(vertical().sty("background-color", "gray"));
        }
        // Page layout
        ly.add(text("Page layout").sizeXXL());
        int[] data = new int[10];
        for (int i = 0; i < 10; i++) {
            data[i] = i;
        }
        ly.add(
                pagelayout().childGap(true).setDataProvider(0, 3, (details) -> {
                    List<Component<?,?>> comps = new ArrayList<>();
                    for (int i = Math.max(details.iStart, 0); i < Math.min(details.iEnd, data.length - 1); i++) {
                        comps.add(text("Index: "+data[i]).width("100%").sty("background-color", "lightgray"));
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
        ly.add(link().setValue(About.class).add(text("Go to About page!")));
        ly.add(link().setValue("/about").add(text("Go to About page!")));
        ly.add(link().setValue("https://google.com").add(text("Go to Google page!")));

        //
        // Overlays
        //
        ly.add(text("Overlays").sizeXXL());
        ly.add(overlay(null).add(text("Overlay over the page")));
        ly.verticalCL().sty("background-color", "blue").size("100px", "100px").add(
                overlay(ly.lastChild()).sty("background-color", "red")
                        .add(text("Overlay over another component.")));
        ly.add(button("Show Popup").onClick(e -> {
            var p = new Popup();
            p.add(text("Hello from inside an popup!"));
            ly.add(p);
        }));
        ly.add(button("Hover over me!").setTooltip("This is a tooltip!"));

        //
        // Inputs
        //
        ly.add(text("Inputs").sizeXXL());
        AtomicInteger i = new AtomicInteger();
        ly.horizontalCL().childGap(true).width("100%")
                .add(button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.label.setValue(s);
        }), button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.label.setValue(s);
        }), button("Click me!").grow(1).onClick(e -> {
            String s = "Clicked " + i.incrementAndGet() + " times";
            AL.info(s);
            e.comp.label.setValue(s);
        }));
        // Button variants
        ly.horizontalCL().childGap(true).add(button("Primary").primary(), button("Secondary").secondary(), button("Success").success(),
                button("Danger").danger(), button("Warning").warning(), button("Info").info(),
                button("Light").light(), button("Dark").dark());
        // Fields
        var tf = textfield("Text field label", "Def").onValueChange(e -> {
            AL.info("Input of textfield changed: "+e.value+" before: "+e.valueBefore);
            AL.info("Or via getValue() = "+e.comp.getValue());
        });

        String utf8TestString = "Yearly / Jährlich / Árlega / Ročně / Évente / Yıllık / Ετησίως / Ежегодно / שנתי / سنويًا / 年間 / 연간 / 每年 / รายปี / वार्षिक / Hàng năm / წლიური / Տարեկան";
        ly.horizontalCL().add(tf, button("Insert from Java").onClick(e -> {
            tf.setValue(utf8TestString);
            tf.getValue(val -> {
               if(!val.equals(utf8TestString))
                   AL.warn("UTF 8 TEST STRING RECEIVED FROM BROWSER IS NOT EQUAL TO STRING IN JAVA BACKEND!!!");
            });
        }));
        ly.add(textarea("Text area label", "Def").onValueChange(e -> {
            AL.info("Input of textarea changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(passwordfield("Password field label").onValueChange(e -> {
            AL.info("Input of passwordfield changed: "+e.value+" before: "+e.valueBefore);
        }).setValue("\\path").setValue(null)); // setting to null changes its value to ""
        ly.add(checkbox("Checkbox label").onValueChange(e -> {
            AL.info("Input of checkbox changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(colorpicker("Color picker label").onValueChange(e -> {
            AL.info("Input of colorpicker changed: "+e.value+" before: "+e.valueBefore);
        }));
        ly.add(slider("Slider label").onValueChange(e -> {
            AL.info("Input of slider changed: "+e.value+" before: "+e.valueBefore);
        }));
        // Option field
        // You can also add strings directly instead of components
        ly.add(optionfield("Option field label")
                .addItems(text("Option 1"), text("Option 2"), text("Option 3"))
                .onValueChange(e -> {
            AL.info("Input of option field changed: "+e.value+" before: "+e.valueBefore);
        }));
        // File uploader (probably only relevant if this a web server)
        ly.add(fileuploader("File uploader label").onValueChange(e -> {
            AL.info("Input of file uploader changed: "+e.value +" before: "+e.valueBefore);
            // File content can be accessed via e.content (byte array)
        }));

        //
        // File chooser
        //
        ly.add(text("File chooser/selector").sizeXXL());
        ly.add(filechooser("File chooser label").setDir(App.workingDir).onFileSelected(e -> {
            AL.info("Selected a file: "+e.file);
            // List of all selected files: e.directoryView.selectedFiles
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

        //
        // Icons (fontawesome)
        //
        ly.add(text("Icons").sizeXXL());
        ly.horizontalCL().add(Icon.regular_bell(), Icon.regular_bookmark(), Icon.regular_heart());
        ly.horizontalCL().add(Icon.solid_bell(), Icon.solid_bookmark(), Icon.solid_heart());
        // Default sizes can be changed in App.theme

        //
        // UI
        //
        ly.add(text("UI/Window").sizeXXL());
        ly.horizontalCL().childSpaceEvenly1().add(
                checkbox("maximize").onValueChange(e -> {
                    try{
                        UI.get().maximize(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("minimize").onValueChange(e -> {
                    try{
                        UI.get().minimize(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("fullscreen").onValueChange(e -> {
                    try{
                        UI.get().fullscreen(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("decorate").onValueChange(e -> {
                    try{
                        UI.get().decorate(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("allwaysOnTop").onValueChange(e -> {
                    try{
                        UI.get().allwaysOnTop(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("focus").onValueChange(e -> {
                    try{
                        UI.get().focus(e.value);
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                }),
                checkbox("transparent background").onValueChange(e -> {
                    try{
                        if(e.value)UI.get().background("#00000000");
                        else UI.get().background("#FFFFFFFF");
                    } catch (Exception ex) {
                        AL.warn(ex);
                        e.comp.label.setValue("Failed, see log for details. "+ex.getMessage());
                    }
                })
        );

        //
        // Performance tests
        //
        ly.add(text("Performance tests").sizeXXL());
        AtomicLong ms = new AtomicLong(System.currentTimeMillis());
        TextField numElements = textfield("Enter a number of how many elements to add", "100");
        Horizontal lyElements = horizontal().scrollable(true, "100vw", "100px", "10px", "10px");
        Button btnAddElements = button("Click to add elements below").onClick(e -> {
            ms.set(System.currentTimeMillis());
            int size = Integer.parseInt(numElements.getValue());
            for (int i1 = 0; i1 < size; i1++) {
                lyElements.add(image(Home.class,"/images/desku_banner.png"));
            }
            long msTook = System.currentTimeMillis() - ms.get();
            e.comp.label.setValue("Click to add elements below (took "+msTook+"ms, total elements: "+lyElements.children.size()+")");
        });
        Button btnClearElements = button("Clear").onClick(e -> {
            btnAddElements.label.setValue("Click to add elements below");
            lyElements.removeAll();
        });

        ly.add(
                numElements,
                horizontal().padding(false)
                        .add(btnAddElements, btnClearElements),
                lyElements
        );

        ly.horizontalCL().width("100%")
                .add(text("Start").sty("background", "red"))
                .add(text("Grow").grow(1).sty("background", "blue"))
                .add(text("End").sty("background", "red").selfEnd1());

        return ly;
    }
}
