package com.osiris.desku.ui.input;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.TextChangeEvent;
import com.osiris.desku.ui.layout.ListLayout;
import com.osiris.events.Event;

import java.util.function.Consumer;

public class Selector extends Component<Selector> {

    // Layout
    public Text label;
    public Button button;
    public ListLayout items = new ListLayout().visible(false);

    // Events
    public Event<TextChangeEvent<Selector>> _onValueChange = new Event<>();

    public Selector() {
        this("", "");
    }

    public Selector(String label) {
        this(label, "");
    }

    public Selector(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public Selector(Text label, String defaultValue) {
        addClass("dropdown");
        this.label = label;
        this.button = new Button(defaultValue).secondary()
                .childCenter().childCenter2().childGap("0.5vw").addClass("dropdown-toggle")
                .putAttribute("type=", "button").putAttribute("data-bs-toggle", "dropdown")
                .putAttribute("aria-expanded", "false").onClick(e -> {
                    items.visible(!items.isVisible());
                });
        this.items.addClass("dropdown-menu");
        add(this.label, this.button, this.items);
        childVertical();

        // Lastly change add function:
        Consumer<AddedChildEvent> superItemsAdd = this.items._add;
        this._add = e -> {
            e.childComp.addClass("dropdown-item");
            e.childComp.putStyle("cursor", "pointer");
            e.childComp.onClick(click -> {
                items.visible(false);
                setValue(click.comp.element.text());
            });
            superItemsAdd.accept(e); // Directly add children to items / list layout
        };
    }

    public String getValue() {
        return this.button.element.attr("value");
    }

    /**
     * Triggers {@link #_onValueChange} event.
     */
    public Selector setValue(String s) {
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
    public Selector onValueChange(Consumer<TextChangeEvent<Selector>> code) {
        _onValueChange.addAction((event) -> code.accept(event));
        this.button.text.onAddedString.addAction(newValue -> {
            // This event is only triggered from the Java side.
            TextChangeEvent<Selector> e = new TextChangeEvent<>("{\"newValue\": \"" + newValue + "\", \"eventAsJson\": {}}",
                    this, ""); // TODO support value before
            _onValueChange.execute(e); // Executes all listeners
        });
        return _this;
    }


}
