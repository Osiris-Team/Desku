package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.*;
import com.osiris.desku.ui.input.*;
import com.osiris.desku.ui.layout.*;

import java.lang.reflect.Field;
import java.util.function.Predicate;

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
    static public Spinner spinner() {
        return new Spinner();
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
    static public Selector selector() {
        return new Selector();
    }

    /**
     */
    static public Selector selector(String label) {
        return new Selector(label);
    }

    /**
     */
    static public Selector selector(String label, String defaultValue) {
        return new Selector(label, defaultValue);
    }

    /**
     */
    static public Selector selector(Text label, String defaultValue) {
        return new Selector(label, defaultValue);
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
    static public Horizontal horizontal() {
        return new Horizontal();
    }

    /**
     */
    static public ListLayout listlayout() {
        return new ListLayout();
    }

    /**
     * @param parent if null, this overlay will be placed over the complete page,
     *               otherwise only over the provided parent component.
     */
    static public Overlay overlay(Component<?> parent) {
        return new Overlay(parent);
    }

    /**
     */
    static public PageLayout pagelayout() {
        return new PageLayout();
    }

    /**
     * Smart, mobile optimized layout, that aligns items horizontally if there is space
     * or goes to the next line.
     * Items will have at least 400px width, or go beyond that (since flex-grow is set to 1).
     * If the device width is smaller than 400px, the width is set to the device width.
     */
    static public SmartLayout smartlayout() {
        return new SmartLayout();
    }

    /**
     * Smart, mobile optimized layout, that aligns items horizontally if there is space
     * or goes to the next line.
     *
     * @param childGrow     if true child components will try to fill out the complete available space.
     * @param minChildWidth the min width of a child component.
     */
    static public SmartLayout smartlayout(boolean childGrow, String minChildWidth) {
        return new SmartLayout(childGrow, minChildWidth);
    }

    /**
     */
    static public TabLayout tablayout() {
        return new TabLayout();
    }

    /**
     */
    static public Vertical vertical() {
        return new Vertical();
    }
}
