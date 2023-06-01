package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.BooleanChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class CheckBox extends Component<CheckBox> {

    // Layout
    public Text label;
    public Input input = new Input("checkbox");

    // Events
    public Event<BooleanChangeEvent<CheckBox>> _onValueChange = new Event<>();

    public CheckBox() {
        this("", false);
    }

    public CheckBox(String label) {
        this(label, false);
    }

    public CheckBox(String label, boolean defaultValue) {
        this(new Text(label).sizeXS(), defaultValue);
    }

    public CheckBox(Text label, boolean defaultValue) {
        addClass("checkbox");
        this.label = label;
        add(this.input, this.label);
        childGap(true);
        this.input.element.attr("checked", defaultValue); // TODO not sure if this also works in async
    }

    public boolean getValue() {
        return Boolean.parseBoolean(this.input.element.attr("value"));
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public CheckBox setValue(boolean val) {
        this.input.putAttribute("value", String.valueOf(val));
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public CheckBox onValueChange(Consumer<BooleanChangeEvent<CheckBox>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"newValue\": \"` + event.target.checked + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    BooleanChangeEvent<CheckBox> e = new BooleanChangeEvent<>(msg, this, getValue());
                    input.element.attr("value", String.valueOf(e.value)); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
