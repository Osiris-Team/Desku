package com.osiris.desku.ui.display;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.UI;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
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
        super(s, "txt");
        setValue(s);
        // Attach Java event listeners
        UI win = UI.get();
        Runnable registration = () -> {
            _onValueAppended.addAction((childString) -> {
                win.executeJavaScript(win.jsGetComp("comp", id) +
                                "var childString = document.createTextNode(`" + childString + "`);\n" +
                                "comp.appendChild(childString);\n",
                        "internal", 0);
            });
            _onEmptyValue.addAction((_void) -> {
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

    public Text append(String oldValue, String valueToAppend) {
        element.appendText(valueToAppend);
        _onValueAppended.execute(valueToAppend); // Updates the UI
        String newValue = oldValue + valueToAppend;
        super.setValue(newValue);
        return this;
    }

    public Text sizeXS() {
        s("font-size", "var(--font-size-xs)");
        return this;
    }

    public Text sizeS() {
        s("font-size", "var(--font-size-s)");
        return this;
    }

    public Text sizeM() {
        s("font-size", "var(--font-size-m)");
        return this;
    }

    public Text sizeL() {
        s("font-size", "var(--font-size-l)");
        return this;
    }

    public Text sizeXL() {
        s("font-size", "var(--font-size-xl)");
        return this;
    }

    public Text sizeXXL() {
        s("font-size", "var(--font-size-xxl)");
        return this;
    }

    public Text sizeXXXL() {
        s("font-size", "var(--font-size-xxxl)");
        return this;
    }

    public Text bold() {
        s("font-weight", "bold");
        return this;
    }

    public Text bolder() {
        s("font-weight", "bolder");
        return this;
    }
}
