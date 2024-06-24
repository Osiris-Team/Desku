package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class TextField extends Component<TextField, String> {

    // Layout
    public Text label;
    public String defaultValue;
    public Input<String> input;

    public TextField() {
        this("", "");
    }

    public TextField(String label) {
        this(label, "");
    }

    public TextField(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public TextField(Text label, String defaultValue) { // TODO defaultValue is not visible in UI
        super(defaultValue);
        addClass("textfield");
        addClass("input-group");
        this.label = label;
        this.defaultValue = defaultValue;
        this.input = new Input<>("text", defaultValue);
        add(this.label, this.input);
        childVertical();
    }

    public String getValue() {
        return this.input.element.attr("value");
    }

    public TextField setValue(String v) {
        input.setValue(v);
        return this;
    }

    @Override
    public TextField getValue(Consumer<String> v) {
        input.getValue(v);
        return this;
    }

    @Override
    public TextField onValueChange(Consumer<ValueChangeEvent<TextField, String>> code) {
        // Forward input text change event to this component
        input.onValueChange(e -> {
            ValueChangeEvent<TextField, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
        });
        return this;
    }


}
