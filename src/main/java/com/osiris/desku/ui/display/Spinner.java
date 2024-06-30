package com.osiris.desku.ui.display;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

public class Spinner extends Component<Spinner, NoValue> {
    public Spinner() {
        super(NoValue.GET, NoValue.class);
        typeBorder();
        primary();
    }

    // TYPES

    public Spinner typeBorder() {
        addClass("spinner-border");
        return this;
    }

    public Spinner typeGrow() {
        addClass("spinner-grow");
        return this;
    }

    // SIZES

    public Spinner sizeS() {
        addClass("spinner-border-sm");
        return this;
    }

    public Spinner sizeM() {
        removeClass("spinner-border-sm");
        removeClass("spinner-border-lg");
        return this;
    }

    public Spinner sizeL() {
        addClass("spinner-border-lg");
        return this;
    }

    // VARIANTS

    public Spinner primary() {
        addClass("text-primary");
        return this;
    }

    public Spinner secondary() {
        addClass("text-secondary");
        return this;
    }

    public Spinner success() {
        addClass("text-success");
        return this;
    }

    public Spinner danger() {
        addClass("text-danger");
        return this;
    }

    public Spinner warning() {
        addClass("text-warning");
        return this;
    }

    public Spinner info() {
        addClass("text-info");
        return this;
    }

    public Spinner light() {
        addClass("text-light");
        return this;
    }

    public Spinner dark() {
        addClass("text-dark");
        return this;
    }
}
