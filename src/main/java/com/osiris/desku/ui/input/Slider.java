package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class Slider extends Component<Slider, Double> {

    // Layout
    public Text label;
    public Input<Double> input;

    public Slider() {
        this("", 0.0);
    }

    public Slider(String label) {
        this(label, 0.0);
    }

    public Slider(String label, double defaultValue) {
        this(new Text(label).sizeS(), defaultValue, 0.0, 100.0, 1.0);
    }

    public Slider(Text label, double defaultValue, double minValue, double maxValue, double stepValue) {
        super(defaultValue, Double.class);
        addClass("slider");
        this.label = label;
        this.input = new Input<>("range", defaultValue);
        add(this.label, this.input);
        childVertical();
        this.input.atr("value", String.valueOf(defaultValue));
        this.input.atr("min", String.valueOf(minValue));
        this.input.atr("max", String.valueOf(maxValue));
        this.input.atr("step", String.valueOf(stepValue));
    }

    @Override
    public Slider getValue(Consumer<Double> v) {
        input.getValue(v);
        return this;
    }


    public Slider setValue(Double v) {
        input.setValue(v);
        return this;
    }


    @Override
    public Slider onValueChange(Consumer<ValueChangeEvent<Slider, Double>> code) {
        // Forward input text change event to this component
        input.onValueChange(e -> {
            ValueChangeEvent<Slider, Double> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
        });
        return this;
    }

}
