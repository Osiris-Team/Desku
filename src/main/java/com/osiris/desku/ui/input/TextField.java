package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class TextField extends Component<TextField> {
    public final Event<TextChangeEvent> _onValueChange = new Event<>();

    public TextField() {
        super("input");
        putAttribute("class", "textfield");
        putAttribute("slot", "input");
        putAttribute("type", "text");
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public TextField onValueChange(Consumer<TextChangeEvent<TextField>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", _this, "message = `{\"changedText\": \"` + event.target.value + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    _onValueChange.execute(new TextChangeEvent<>(msg, _this)); // Executes all listeners
                });
        return _this;
    }
}
