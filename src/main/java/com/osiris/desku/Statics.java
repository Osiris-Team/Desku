package com.osiris.desku;

import com.osiris.desku.ui.display.Image;
import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;
import java.io.File;
import java.nio.file.Files;
import com.osiris.desku.ui.display.Router;
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
import com.osiris.desku.ui.input.CheckBox;
import com.osiris.desku.ui.event.BooleanChangeEvent;
import com.osiris.desku.ui.input.ColorPicker;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.desku.ui.input.Input;
import com.osiris.desku.ui.input.PasswordField;
import com.osiris.desku.ui.input.Slider;
import com.osiris.desku.ui.event.DoubleChangeEvent;
import com.osiris.desku.ui.input.TextField;
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
     */
    static public Router router() {
        return new Router();
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
     * https://getbootstrap.com/docs/5.3/components/buttons/
     */
    static public Button button(String txt) {
        return new Button(txt);
    }

    /**
     */
    static public CheckBox checkbox() {
        return new CheckBox();
    }

    /**
     */
    static public CheckBox checkbox(String label) {
        return new CheckBox(label);
    }

    /**
     */
    static public CheckBox checkbox(String label, boolean defaultValue) {
        return new CheckBox(label, defaultValue);
    }

    /**
     */
    static public CheckBox checkbox(Text label, boolean defaultValue) {
        return new CheckBox(label, defaultValue);
    }

    /**
     */
    static public ColorPicker colorpicker() {
        return new ColorPicker();
    }

    /**
     */
    static public ColorPicker colorpicker(String label) {
        return new ColorPicker(label);
    }

    /**
     */
    static public ColorPicker colorpicker(String label, String defaultValue) {
        return new ColorPicker(label, defaultValue);
    }

    /**
     */
    static public ColorPicker colorpicker(Text label, String defaultValue) {
        return new ColorPicker(label, defaultValue);
    }

    /**
     */
    static public Input input(String type) {
        return new Input(type);
    }

    /**
     */
    static public PasswordField passwordfield() {
        return new PasswordField();
    }

    /**
     */
    static public PasswordField passwordfield(String label) {
        return new PasswordField(label);
    }

    /**
     */
    static public PasswordField passwordfield(String label, String defaultValue) {
        return new PasswordField(label, defaultValue);
    }

    /**
     */
    static public PasswordField passwordfield(Text label, String defaultValue) {
        return new PasswordField(label, defaultValue);
    }

    /**
     */
    static public Slider slider() {
        return new Slider();
    }

    /**
     */
    static public Slider slider(String label) {
        return new Slider(label);
    }

    /**
     */
    static public Slider slider(String label, double defaultValue) {
        return new Slider(label, defaultValue);
    }

    /**
     */
    static public Slider slider(Text label, double defaultValue, double minValue, double maxValue, double stepValue) {
        return new Slider(label, defaultValue, minValue, maxValue, stepValue);
    }

    /**
     */
    static public TextField textfield() {
        return new TextField();
    }

    /**
     */
    static public TextField textfield(String label) {
        return new TextField(label);
    }

    /**
     */
    static public TextField textfield(String label, String defaultValue) {
        return new TextField(label, defaultValue);
    }

    /**
     */
    static public TextField textfield(Text label, String defaultValue) {
        return new TextField(label, defaultValue);
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
