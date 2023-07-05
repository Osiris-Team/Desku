package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class TextField extends Component<TextField> {

    // Layout
    public Text label;
    public String defaultValue;
    public Input input = new Input("text");

    // Events
    public Event<TextChangeEvent<TextField>> _onValueChange = new Event<>();

    public TextField() {
        this("", "");
    }

    public TextField(String label) {
        this(label, "");
    }

    public TextField(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public TextField(Text label, String defaultValue) {
        addClass("textfield");
        this.label = label;
        this.defaultValue = defaultValue;
        add(this.label, this.input);
        childVertical();
        this.input.putAttribute("value", defaultValue);
    }

    public String getValue() {
        return this.input.element.attr("value");
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public TextField setValue(String s) {
        this.input.putAttribute("value", s);
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public TextField onValueChange(Consumer<TextChangeEvent<TextField>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"newValue\": \"` + event.target.value + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    TextChangeEvent<TextField> e = new TextChangeEvent<>(msg, this, input.element.attr("value"));
                    input.element.attr("value", e.value); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
