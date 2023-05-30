package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class TextField extends Component<TextField> {

    // Layout
    public Text label;
    public Input input = new Input();

    // Events
    public Event<TextChangeEvent> _onValueChange = new Event<>();

    public TextField(){
        this("", "");
    }

    public TextField(String label){
        this(label, "");
    }

    public TextField(String label, String defaultValue){
        this(new Text(label).sizeXS(), defaultValue);
    }

    public TextField(Text label, String defaultValue) {
        super("textfield");
        this.label = label;
        add(this.label, this.input);
        this.input.putAttribute("value", defaultValue);
    }

    public String getValue(){
        return this.input.element.attr("value");
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public TextField onValueChange(Consumer<TextChangeEvent<TextField>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"changedText\": \"` + event.target.value + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    TextChangeEvent<Input> e = new TextChangeEvent<>(msg, input, input.element.attr("value"));
                    input.element.attr("value", e.value); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }

    public class Input extends Component<Input>{

        public Input() {
            super("input");
            putAttribute("class", "textfield");
            putAttribute("slot", "input");
            putAttribute("type", "text");
        }
    }


}