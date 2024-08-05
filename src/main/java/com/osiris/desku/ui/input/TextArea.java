package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class TextArea extends Component<TextArea, String> {

    // Layout
    public Text label;
    public String defaultValue;
    public Input<String> input;

    public TextArea() {
        this("", "");
    }

    public TextArea(String label) {
        this(label, "");
    }

    public TextArea(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public TextArea(Text label, String defaultValue) { // TODO defaultValue is not visible in UI
        super(defaultValue, String.class);
        addClass("input-group");
        this.label = label;
        this.defaultValue = defaultValue;
        this.input = new Input<>("textarea", defaultValue, String.class, "textarea");
        add(this.label, this.input);
        childVertical();
    }

    public TextArea setValue(String v) {
        input.setValue(v);
        return this;
    }

    @Override
    public void getValueInternal(Consumer<String> v) {
        input.getValue(v);
    }

    @Override
    public TextArea onValueChange(Consumer<ValueChangeEvent<TextArea, String>> code) {
        // Forward input text change event to this component
        input.onValueChange(e -> {
            ValueChangeEvent<TextArea, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
        });
        return this;
    }


}
