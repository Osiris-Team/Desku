package com.osiris.desku.ui;

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

}
