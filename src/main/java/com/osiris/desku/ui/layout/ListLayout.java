package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;

import java.util.function.Consumer;

public class ListLayout extends Component<ListLayout, NoValue> {

    public ListLayout() {
        super(NoValue.GET, NoValue.class, "ul");
        Consumer<AddedChildEvent> superAdd = _add;
        _add = e -> {
            e.childComp.element.tagName("li"); // Set tag name before adding child
            superAdd.accept(e);
        };
    }
}
