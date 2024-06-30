package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

public class Horizontal extends Component<Horizontal, NoValue> {
    public Horizontal() {
        super(NoValue.GET, NoValue.class);
        childHorizontal();
    }
}
