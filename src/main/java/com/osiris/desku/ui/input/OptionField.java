package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.desku.ui.layout.SmartLayout;
import com.osiris.desku.utils.GodIterator;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class OptionField extends Component<OptionField> {

    // Layout
    public Text label;
    public Button button;
    public SmartLayout items = new SmartLayout();

    // Events
    public Event<TextChangeEvent<OptionField>> _onValueChange = new Event<>();

    public OptionField() {
        this("", "");
    }

    public OptionField(String label) {
        this(label, "");
    }

    public OptionField(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public OptionField(Text label, String defaultValue) {
        if(defaultValue == null || defaultValue.isEmpty()) defaultValue = "Select";
        this.label = label;
        this.button = new Button(defaultValue).secondary()
                .width("100%")
                .childStart().childGap("0.5vw").onClick(e -> {
                    items.visible(!items.isVisible());
                });
        this.items.visible(false);
        add(this.label, this.button, this.items);
        childVertical();

        // Lastly change add function:
        Consumer<AddedChildEvent> superItemsAdd = this.items._add;
        this._add = e -> {
            e.childComp.putStyle("cursor", "pointer");
            e.childComp.onClick(click -> {
                items.visible(false);
                set(click.comp.element.text());
            });
            superItemsAdd.accept(e); // Directly add children to items / list layout
        };
    }

    public OptionField add(String... options) {
        if (options == null) return _this;
        GodIterator.forEach(options, s -> {
            _add.accept(new AddedChildEvent(new Text(s), null, false, false));
        });
        return _this;
    }

    public String get() {
        return this.button.element.attr("value");
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public OptionField set(String s) {
        this.button.putAttribute("value", s);
        this.button.text.set(s);
        return this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, String, Consumer)
     */
    public OptionField onValueChange(Consumer<TextChangeEvent<OptionField>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        this.button.text.onValueChanged(e -> {
            // This event is only triggered from the Java side.
            TextChangeEvent<OptionField> textChangeEvent = new TextChangeEvent<>("{\"newValue\": \"" + e.value + "\", \"eventAsJson\": {}}",
                    this, e.valueBefore);
            this.button.element.attr("value", e.value); // Update in-memory value
            _onValueChange.execute(textChangeEvent); // Executes all listeners
        });
        return _this;
    }


}
