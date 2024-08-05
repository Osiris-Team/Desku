package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class PasswordField extends Component<PasswordField, String> {

    // Layout
    public Text label;
    public Input<String> input;

    public PasswordField() {
        this("", "");
    }

    public PasswordField(String label) {
        this(label, "");
    }

    public PasswordField(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public PasswordField(Text label, String defaultValue) {
        super(defaultValue, String.class);
        addClass("passwordfield");
        addClass("input-group");
        this.label = label;
        this.input = new Input<>("password", defaultValue, String.class);
        add(this.label, this.input);
        childVertical();
    }

    @Override
    public PasswordField getValue(Consumer<String> v) {
        input.getValue(v);
        return _this;
    }


    public PasswordField setValue(String v) {
        input.setValue(v);
        return this;
    }


    @Override
    public PasswordField onValueChange(Consumer<ValueChangeEvent<PasswordField, String>> code) {
        // Forward input text change event to this component
        input.onValueChange(e -> {
            ValueChangeEvent<PasswordField, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
        });
        return this;
    }


}
