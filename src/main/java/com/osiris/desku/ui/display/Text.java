package com.osiris.desku.ui.display;

import com.osiris.desku.UI;
import com.osiris.desku.ui.Component;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import org.jsoup.nodes.TextNode;

public class Text extends Component<Text> {
    /**
     * Executed when a child was added on the Java side.
     */
    public final Event<String> onAddedString = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Void> onRemovedAllStrings = new Event<>();

    @Override
    public void init(Text target, String tag) {
        super.init(target, tag);
        // Attach Java event listeners
        UI win = UI.get();
        Runnable registration = () -> {
            onAddedString.addAction((childString) -> {
                win.browser.executeJavaScript(win.jsGetComp("comp", id) +
                                "var childString = document.createTextNode(`" + childString + "`);\n" +
                                "comp.appendChild(childString);\n",
                        "internal", 0);
            });
            onRemovedAllStrings.addAction((_void) -> {
                win.browser.executeJavaScript(win.jsGetComp("comp", id) +
                                "comp.textContent = '';\n", // remove all text nodes
                        "internal", 0);
            });
        };
        if(!win.isLoading) registration.run();
        else win.onLoadStateChanged.addAction((action, event) -> {
            if(event.isLoading) return;
            action.remove();
            registration.run();
        }, AL::warn);
    }

    public Text(String s) {
        init(this, "txt");
        append(s);
    }

    public String get() {
        return element.text();
    }

    public Text set(String s){
        clear();
        append(s);
        return this;
    }

    public Text clear(){
        for (TextNode txt : element.textNodes()) {
            txt.remove(); // Remove all text nodes from parent
        }
        onRemovedAllStrings.execute(null); // Updates the UI
        return this;
    }

    public Text append(String s) {
        element.appendText(s);
        onAddedString.execute(s); // Updates the UI
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
