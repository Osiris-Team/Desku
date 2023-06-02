package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class PasswordField extends Component<PasswordField> {

    // Layout
    public Text label;
    public Input input = new Input("password");

    // Events
    public Event<TextChangeEvent<PasswordField>> _onValueChange = new Event<>();

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
        addClass("passwordfield");
        this.label = label;
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
    public PasswordField setValue(String val) {
        this.input.putAttribute("value", val);
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public PasswordField onValueChange(Consumer<TextChangeEvent<PasswordField>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"newValue\": \"` + event.target.value + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    TextChangeEvent<PasswordField> e = new TextChangeEvent<>(msg, this, input.element.attr("value"));
                    input.element.attr("value", e.value); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
