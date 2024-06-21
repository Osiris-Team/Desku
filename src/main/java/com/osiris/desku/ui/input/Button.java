package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;

import java.util.function.Consumer;

public class Button extends Component<Button, String> {

    public final Text label;

    /**
     * https://getbootstrap.com/docs/5.3/components/buttons/
     */
    public Button(String txt) {
        super(txt, "button");
        this.label = new Text(txt).sty("color", "var(--color-contrast)");
        add(label);
        childCenter1();
        addClass("btn");
        primary();
    }

    /**
     * @return button label as string.
     */
    @Override
    public Button getValue(Consumer<String> v) {
        label.getValue(v);
        return this;
    }

    /**
     * Set button label string.
     */
    public Button setValue(String v) {
        label.setValue(v);
        return this;
    }

    /**
     * Executed when button label string changed.
     */
    @Override
    public Button onValueChange(Consumer<ValueChangeEvent<Button, String>> code) {
        // Forward label text change event to this button
        label.onValueChange(e -> {
            ValueChangeEvent<Button, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore);
            code.accept(e2);
        });
        return this;
    }

    // SIZES
    public Button sizeS() {
        addClass("btn-sm");
        return this;
    }

    public Button sizeM() {
        removeClass("btn-sm");
        removeClass("btn-lg");
        return this;
    }

    public Button sizeL() {
        addClass("btn-lg");
        return this;
    }

    // VARIANTS

    public Button primary() {
        addClass("btn-primary");
        return this;
    }

    public Button secondary() {
        addClass("btn-secondary");
        return this;
    }

    public Button success() {
        addClass("btn-success");
        return this;
    }

    public Button danger() {
        addClass("btn-danger");
        return this;
    }

    public Button warning() {
        addClass("btn-warning");
        return this;
    }

    public Button info() {
        addClass("btn-info");
        return this;
    }

    public Button light() {
        addClass("btn-light");
        return this;
    }

    public Button dark() {
        addClass("btn-dark");
        return this;
    }

    // VARIANTS OUTLINED

    public Button primaryOutlined() {
        addClass("btn-outline-primary");
        return this;
    }

    public Button secondaryOutlined() {
        addClass("btn-outline-secondary");
        return this;
    }

    public Button successOutlined() {
        addClass("btn-outline-success");
        return this;
    }

    public Button dangerOutlined() {
        addClass("btn-outline-danger");
        return this;
    }

    public Button warningOutlined() {
        addClass("btn-outline-warning");
        return this;
    }

    public Button infoOutlined() {
        addClass("btn-outline-info");
        return this;
    }

    public Button lightOutlined() {
        addClass("btn-outline-light");
        return this;
    }

    public Button darkOutlined() {
        addClass("btn-outline-dark");
        return this;
    }

    // OTHER
}
