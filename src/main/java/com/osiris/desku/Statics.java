package com.osiris.desku;

import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;
import java.io.File;
import java.nio.file.Files;
import com.osiris.desku.ui.display.RTable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import com.osiris.desku.ui.display.Table;
import java.io.IOException;
import java.util.function.Consumer;
import com.osiris.desku.ui.display.Text;
import com.osiris.events.Event;
import org.jsoup.nodes.TextNode;
import com.osiris.desku.ui.input.Button;
import com.osiris.desku.ui.layout.Layout;
import com.osiris.desku.ui.layout.Overlay;

/**
 * Automatically generated class. To re-generate/update <br>
 * execute this in your console: ./gradlew build :test --tests "com.osiris.desku.GenerateStatics"
 */
public class Statics {

    /**
     * Java integration of the HTML img tag. <br>
     * <a href="https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img">https://developer.mozilla.org/en-US/docs/Web/HTML/Element/img</a>
     *
     * @param clazz image must be in the same package as this class or in a sub-package/sub-directory of this package.
     *              See {@link App#getResourceInPackage(Package, String)} for more details.
     * @param src   examples: "/image.png" or "image.png" or "/sub-dir/image.png" or "sub-dir/image.png"
     */
    static public Image image(Class<?> clazz, String src) {
        return new Image(clazz, src);
    }

    /**
     * @param clazz uses this classes' fields for the headers.
     */
    static public RTable rtable(Class<?> clazz) {
        return new RTable(clazz);
    }

    /**
     * @param clazz          uses this classes' fields for the headers.
     * @param fieldPredicate is used to determine which fields to use.
     */
    static public RTable rtable(Class<?> clazz, Predicate<Field> fieldPredicate) {
        return new RTable(clazz, fieldPredicate);
    }

    /**
     */
    static public Table table() {
        return new Table();
    }

    /**
     */
    static public Text text(String s) {
        return new Text(s);
    }

    /**
     */
    static public Button button(String txt) {
        return new Button(txt);
    }

    /**
     */
    static public Layout layout() {
        return new Layout();
    }

    /**
     */
    static public Layout layout(boolean isHorizontal) {
        return new Layout(isHorizontal);
    }

    /**
     * @param parent if null, this overlay will be placed over the complete page,
     *               otherwise only over the provided parent component.
     */
    static public Overlay overlay(Component<?> parent) {
        return new Overlay(parent);
    }
}
