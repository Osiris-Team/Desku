package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class CheckBox extends Component<CheckBox, Boolean> {

    // Layout
    public Text label;
    public Input<Boolean> input = new Input<>("checkbox", false)
            .addClass("form-check-input").addClass("mt-0").sty("max-width", "20px");

    public CheckBox() {
        this("", false);
    }

    public CheckBox(String label) {
        this(label, false);
    }

    public CheckBox(String label, boolean defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public CheckBox(Text label, boolean defaultValue) {
        super(defaultValue, Boolean.class);
        addClass("input-group");
        this.label = label;
        add(this.input, this.label);
        childGap(true);
        childCenter2();
    }

    @Override
    public CheckBox getValue(Consumer<Boolean> v) {
        gatr("checked", value -> {
            if (value.isEmpty()) v.accept(false);
            else v.accept(true);
        });
        return _this;
    }

    @Override
    public CheckBox setValue(Boolean v) {
        input.setValue(v);
        if (v) input.atr("checked");
        else input.ratr("checked");
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    @Override
    public CheckBox onValueChange(Consumer<ValueChangeEvent<CheckBox, Boolean>> code) {
        // Custom implementation of onValueChange, because
        readOnlyOnValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"newValue\": \"` + event.target.checked + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    ValueChangeEvent<CheckBox, Boolean> e = new ValueChangeEvent<>(msg, this, internalValue);
                    // Change in memory value, without triggering another change event
                    this.internalValue = e.value;
                    input.element.attr("value", String.valueOf(e.value));
                    if (e.value) input.element.attr("checked", "");
                    else input.element.removeAttr("checked");
                    readOnlyOnValueChange.execute(e); // Executes all listeners
                });
        return this;
    }

}
