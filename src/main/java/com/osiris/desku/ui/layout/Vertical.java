package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

public class Vertical extends Component<Vertical, NoValue> {

    public Vertical() {
        super(NoValue.GET, NoValue.class);
        childVertical();
    }
}
