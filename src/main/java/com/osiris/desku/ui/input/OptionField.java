package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ValueChangeEvent;
import com.osiris.desku.ui.layout.Popup;
import com.osiris.desku.utils.GodIterator;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class OptionField extends Component<OptionField, String> {
    public static class Items extends Popup{
        public Items(OptionField optionField) {
            // Lastly change add function:
            Consumer<AddedChildEvent> superItemsAdd = _add;
            _add = e -> {
                e.childComp.sty("cursor", "pointer");
                e.childComp.onClick(click -> {
                    visible(false);
                    optionField.setValue(click.comp.element.text());
                });
                superItemsAdd.accept(e); // Directly add children to items / list layout
            };
        }

        public Items add(String... options) {
            if (options == null) return this;
            GodIterator.forEach(options, s -> {
                _add.accept(new AddedChildEvent(new Text(s), null, false, false));
            });
            return this;
        }

        public CopyOnWriteArrayList<Component> get(){
            return body.children;
        }
    }

    // Layout
    public Text label;
    public Button button;
    public Items items = new Items(this);
    {
        items.visible(false);
        items.btn1.visible(false);
        items.btn2.visible(false);
    }

    public OptionField() {
        this("", "Select");
    }

    public OptionField(String label) {
        this(label, "Select");
    }

    public OptionField(String label, String defaultValue) {
        this(new Text(label).sizeS(), defaultValue);
    }

    public OptionField(Text label, String defaultValue) {
        super(defaultValue, String.class);
        addClass("input-group");
        this.label = label;
        this.button = new Button(defaultValue)
                .width("100%")
                .childStart1().childGap("0.5vw").onClick(e -> {
                    items.visible(!items.isVisible());
                });
        this.button.label.sty("color", "");
        this.button.removeAllClasses().addClass("form-select");
        this.items.visible(false);
        add(this.label, this.button, this.items);
        childVertical();
    }

    public OptionField addItems(String... arr) {
        items.add(arr);
        return this;
    }

    public OptionField addItems(Component<?, ?>... arr) {
        items.add(arr);
        return this;
    }

    @Override
    public OptionField getValue(Consumer<String> v) {
        button.label.getValue(v);
        return this;
    }


    public OptionField setValue(String v) {
        button.label.setValue(v);
        return this;
    }


    @Override
    public OptionField onValueChange(Consumer<ValueChangeEvent<OptionField, String>> code) {
        // Forward input text change event to this component
        button.label.onValueChange(e -> {
            ValueChangeEvent<OptionField, String> e2 = new ValueChangeEvent<>(e.rawJSMessage, e.jsMessage, this, e.value, e.valueBefore, e.isProgrammatic);
            code.accept(e2);
        });
        return this;
    }


}
