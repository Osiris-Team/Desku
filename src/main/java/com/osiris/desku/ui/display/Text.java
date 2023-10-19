package com.osiris.desku.ui.display;

import com.google.gson.JsonObject;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.desku.ui.event.ValueChangeEvent;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import org.jsoup.nodes.TextNode;

import java.util.function.Consumer;

public class Text extends Component<Text, String> {
    /**
     * Executed when a child was added on the Java side.
     */
    public final Event<String> _onAddedString = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Void> _onRemovedAllStrings = new Event<>();

    public Text(String s) {
        super(s, "txt");
        setValue(s);
        // Attach Java event listeners
        UI win = UI.get();
        Runnable registration = () -> {
            _onAddedString.addAction((childString) -> {
                win.executeJavaScript(win.jsGetComp("comp", id) +
                                "var childString = document.createTextNode(`" + childString + "`);\n" +
                                "comp.appendChild(childString);\n",
                        "internal", 0);
            });
            _onRemovedAllStrings.addAction((_void) -> {
                win.executeJavaScript(win.jsGetComp("comp", id) +
                                "comp.textContent = '';\n", // remove all text nodes
                        "internal", 0);
            });
        };
        if (!win.isLoading.get()) registration.run();
        else win.onLoadStateChanged.addAction((action, isLoading) -> {
            if (isLoading) return;
            action.remove();
            registration.run();
        }, AL::warn);
    }

    @Override
    public Text getValue(Consumer<String> v) {
        // We do not expect the user to change the text, thus we can directly return the internal value
        // which can only be changed programmatically
        v.accept(internalValue);
        return this;
    }

    public Text setValue(String v) {
        String oldValue = internalValue;
        clear();
        append(oldValue, v);
        return this;
    }

    public Text clear() {
        internalValue = "";
        for (TextNode txt : element.textNodes()) {
            txt.remove(); // Remove all text nodes from parent
        }
        _onRemovedAllStrings.execute(null); // Updates the UI
        return this;
    }

    public Text append(String v) {
        append(getValue(), v);
        return this;
    }

    public Text append(String oldV, String v) {
        internalValue += v;
        element.appendText(v);
        _onAddedString.execute(internalValue); // Updates the UI
        JsonObject obj = new JsonObject();
        obj.addProperty("newValue", internalValue);
        obj.add("eventAsJson", new JsonObject());
        readOnlyOnValueChange.execute(new ValueChangeEvent<>("", obj,
                this, internalValue, oldV));
        return this;
    }

    public Text sizeXS() {
        putStyle("font-size", "var(--font-size-xs)");
        return this;
    }

    public Text sizeS() {
        putStyle("font-size", "var(--font-size-s)");
        return this;
    }

    public Text sizeM() {
        putStyle("font-size", "var(--font-size-m)");
        return this;
    }

    public Text sizeL() {
        putStyle("font-size", "var(--font-size-l)");
        return this;
    }

    public Text sizeXL() {
        putStyle("font-size", "var(--font-size-xl)");
        return this;
    }

    public Text sizeXXL() {
        putStyle("font-size", "var(--font-size-xxl)");
        return this;
    }

    public Text sizeXXXL() {
        putStyle("font-size", "var(--font-size-xxxl)");
        return this;
    }

    public Text bold() {
        putStyle("font-weight", "bold");
        return this;
    }

    public Text bolder() {
        putStyle("font-weight", "bolder");
        return this;
    }
}
