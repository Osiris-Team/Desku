package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.DoubleChangeEvent;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class Slider extends Component<Slider> {

    // Layout
    public Text label;
    public Input input = new Input("range");

    // Events
    public Event<DoubleChangeEvent<Slider>> _onValueChange = new Event<>();

    public Slider() {
        this("", 0.0);
    }

    public Slider(String label) {
        this(label, 0.0);
    }

    public Slider(String label, double defaultValue) {
        this(new Text(label).sizeXS(), defaultValue, 0.0, 100.0, 1.0);
    }

    public Slider(Text label, double defaultValue, double minValue, double maxValue, double stepValue) {
        addClass("slider");
        this.label = label;
        add(this.label, this.input);
        childVertical();
        this.input.putAttribute("value", String.valueOf(defaultValue));
        this.input.putAttribute("min", String.valueOf(minValue));
        this.input.putAttribute("max", String.valueOf(maxValue));
        this.input.putAttribute("step", String.valueOf(stepValue));
    }

    public double getValue() {
        return Double.parseDouble(this.input.element.attr("value"));
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public Slider setValue(double val) {
        this.input.putAttribute("value", String.valueOf(val));
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public Slider onValueChange(Consumer<DoubleChangeEvent<Slider>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        UI.get().registerJSListener("input", input, "message = `{\"newValue\": \"` + event.target.value + `\", \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    DoubleChangeEvent<Slider> e = new DoubleChangeEvent<>(msg, this, getValue());
                    input.element.attr("value", String.valueOf(e.value)); // Change in memory value, without triggering another change event
                    _onValueChange.execute(e); // Executes all listeners
                });
        return _this;
    }


}
