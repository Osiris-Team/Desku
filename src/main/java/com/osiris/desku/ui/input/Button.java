package com.osiris.desku.ui.input;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.display.Text;

public class Button extends Component<Button> {

    public final Text text = new Text("Button").putStyle("color", "var(--color-contrast)");

    /**
     * https://getbootstrap.com/docs/5.3/components/buttons/
     */
    public Button(String txt) {
        super("button");
        add(text);
        childCenter();
        text.set(txt);
        putAttribute("class", "btn");
        primary();
    }

    // SIZES
    public Button sizeS() {
        addClass("btn-sm");
        return this;
    }

    public Button sizeM() {
        removeClass("btn-sm");
        removeClass("btn-lg");
        return this;
    }

    public Button sizeL() {
        addClass("btn-lg");
        return this;
    }

    // VARIANTS

    public Button primary() {
        addClass("btn-primary");
        return this;
    }

    public Button secondary() {
        addClass("btn-secondary");
        return this;
    }

    public Button success() {
        addClass("btn-success");
        return this;
    }

    public Button danger() {
        addClass("btn-danger");
        return this;
    }

    public Button warning() {
        addClass("btn-warning");
        return this;
    }

    public Button info() {
        addClass("btn-info");
        return this;
    }

    public Button light() {
        addClass("btn-light");
        return this;
    }

    public Button dark() {
        addClass("btn-dark");
        return this;
    }

    // VARIANTS OUTLINED

    public Button primaryOutlined() {
        addClass("btn-outline-primary");
        return this;
    }

    public Button secondaryOutlined() {
        addClass("btn-outline-secondary");
        return this;
    }

    public Button successOutlined() {
        addClass("btn-outline-success");
        return this;
    }

    public Button dangerOutlined() {
        addClass("btn-outline-danger");
        return this;
    }

    public Button warningOutlined() {
        addClass("btn-outline-warning");
        return this;
    }

    public Button infoOutlined() {
        addClass("btn-outline-info");
        return this;
    }

    public Button lightOutlined() {
        addClass("btn-outline-light");
        return this;
    }

    public Button darkOutlined() {
        addClass("btn-outline-dark");
        return this;
    }

    // OTHER

    public Button enable(boolean b) {
        if (b) removeAttribute("disabled");
        else putAttribute("disabled");
        return this;
    }
}
