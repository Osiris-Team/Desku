package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;

import java.util.function.Consumer;

public class ListLayout extends Component<ListLayout> {

    public ListLayout() {
        super("ul");
        Consumer<AddedChildEvent> superAdd = _add;
        _add = e -> {
            e.childComp = new Element().add(e.childComp); // Wrap around list element before adding child
            superAdd.accept(e);
        };
    }

    public static class Element extends Component<Element>{
        public Element() {
            super("li");
        }
    }
}
