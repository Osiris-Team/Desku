package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class ColorPicker extends Component<ColorPicker, String> {

    // Layout
    public Text label;
    public Input<String> input = new Input<>("color", "#000000");

    public ColorPicker() {
        this("", "#000000");
    }

    public ColorPicker(String label) {
        this(label, "#000000");
    }

    public ColorPicker(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public ColorPicker(Text label, String defaultValue) {
        super(defaultValue);
        addClass("colorpicker");
        this.label = label;
        add(this.label, this.input);
        childVertical();
    }

    @Override
    public ColorPicker getValue(Consumer<String> v) {
        input.getValue(v);
        return this;
    }

    public ColorPicker setValue(String v) {
        input.setValue(v);
        return this;
    }

    @Override
    public ColorPicker onValueChange(Consumer<ValueChangeEvent<ColorPicker, String>> code) {
        input.onValueChange(e -> {
            ValueChangeEvent<ColorPicker, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore);
            code.accept(e2);
        });
        return this;
    }

}
