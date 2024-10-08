package com.osiris.desku.ui.display;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.events.Event;
import org.jetbrains.annotations.Nullable;
import org.jsoup.nodes.TextNode;

public class Text extends Component<Text, String> {
    /**
     * Executed when a child was added on the Java side.
     */
    public final Event<String> _onValueAppended = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Void> _onEmptyValue = new Event<>();

    public Text(String s) {
        super(s, String.class, "txt");
        setValue(s);
        // Attach Java event listeners
        UI ui = UI.get();
        Runnable registration = () -> {
            _onValueAppended.addAction((childString) -> {
                executeJS("var childString = document.createTextNode(`" + childString + "`);\n" +
                                "comp.appendChild(childString);\n");
            });
            _onEmptyValue.addAction((_void) -> {
                executeJS("comp.textContent = '';\n");  // remove all text nodes
                removeAll();
            });
        };
        ui.runIfReadyOrLater(registration);
    }

    public Text setValue(String v) {
        clear();
        append("", v);
        return this;
    }

    public Text clear() {
        internalValue = "";
        for (TextNode txt : element.textNodes()) {
            txt.remove(); // Remove all text nodes from parent
        }
        _onEmptyValue.execute(null); // Updates the UI
        return this;
    }

    public Text append(String v) {
        append(getValue(), v);
        return this;
    }

    public Text append(@Nullable String oldValue, @Nullable String valueToAppend) {
        if(oldValue == null) oldValue = "";
        if(valueToAppend == null) valueToAppend = "";
        element.appendText(valueToAppend);
        _onValueAppended.execute(valueToAppend); // Updates the UI
        String newValue = oldValue + valueToAppend;
        super.setValue(newValue);
        return this;
    }

    public Text sizeXS() {
        sty("font-size", "var(--font-size-xs)");
        return this;
    }

    public Text sizeS() {
        sty("font-size", "var(--font-size-s)");
        return this;
    }

    public Text sizeM() {
        sty("font-size", "var(--font-size-m)");
        return this;
    }

    public Text sizeL() {
        sty("font-size", "var(--font-size-l)");
        return this;
    }

    public Text sizeXL() {
        sty("font-size", "var(--font-size-xl)");
        return this;
    }

    public Text sizeXXL() {
        sty("font-size", "var(--font-size-xxl)");
        return this;
    }

    public Text sizeXXXL() {
        sty("font-size", "var(--font-size-xxxl)");
        return this;
    }

    public Text bold() {
        sty("font-weight", "bold");
        return this;
    }

    public Text bolder() {
        sty("font-weight", "bolder");
        return this;
    }
}
