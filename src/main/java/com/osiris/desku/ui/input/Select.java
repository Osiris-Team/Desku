package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.desku.ui.layout.SmartLayout;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class Select extends Component<Select> {

    // Layout
    public Text label;
    public Button button;
    public SmartLayout items = new SmartLayout();

    // Events
    public Event<TextChangeEvent<Select>> _onSelectedChange = new Event<>();

    public Select() {
        this("", "");
    }

    public Select(String label) {
        this(label, "");
    }

    public Select(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public Select(Text label, String defaultValue) {
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
                setSelected(click.comp.element.text());
            });
            superItemsAdd.accept(e); // Directly add children to items / list layout
        };
    }

    public String getSelected() {
        return this.button.element.attr("value");
    }

    /**
     * Triggers {@link #_onSelectedChange} event.
     */
    public Select setSelected(String s) {
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
    public Select onSelectedChange(Consumer<TextChangeEvent<Select>> code) {
        _onSelectedChange.addAction((event) -> code.accept(event));
        this.button.text.onValueChanged(e -> {
            // This event is only triggered from the Java side.
            TextChangeEvent<Select> textChangeEvent = new TextChangeEvent<>("{\"newValue\": \"" + e.value + "\", \"eventAsJson\": {}}",
                    this, e.valueBefore);
            this.button.element.attr("value", e.value); // Update in-memory value
            _onSelectedChange.execute(textChangeEvent); // Executes all listeners
        });
        return _this;
    }


}
