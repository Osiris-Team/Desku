package com.osiris.desku.ui.display;

import com.osiris.desku.ui.Component;

public class Text extends Component<Text> {
    public Text(String s) {
        init(this, "txt");
        element.appendText(s);
    }

    public String get() {
        return element.text();
    }

    public Text append(String s) {
        element.appendText(s);
        return this;
    }

    public Text sizeXS() {
        style.put("font-size", "var(--font-size-xs)");
        return this;
    }

    public Text sizeS() {
        style.put("font-size", "var(--font-size-s)");
        return this;
    }

    public Text sizeM() {
        style.put("font-size", "var(--font-size-m)");
        return this;
    }

    public Text sizeL() {
        style.put("font-size", "var(--font-size-l)");
        return this;
    }

    public Text sizeXL() {
        style.put("font-size", "var(--font-size-xl)");
        return this;
    }

    public Text sizeXXL() {
        style.put("font-size", "var(--font-size-xxl)");
        return this;
    }

    public Text sizeXXXL() {
        style.put("font-size", "var(--font-size-xxxl)");
        return this;
    }

    public Text bold() {
        style.put("font-weight", "bold");
        return this;
    }

    public Text bolder() {
        style.put("font-weight", "bolder");
        return this;
    }
}
