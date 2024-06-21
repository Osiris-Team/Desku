package com.osiris.desku.ui.layout;

import com.osiris.desku.App;
import com.osiris.desku.ui.Component;
import com.osiris.desku.ui.utils.NoValue;
import com.osiris.jlib.logger.AL;

import java.util.function.Consumer;

public class SmartLayout extends Component<SmartLayout, NoValue> {

    static {
        try {
            App.appendToGlobalCSS(App.getCSS(SmartLayout.class));
        } catch (Exception e) {
            AL.warn(e);
        }
    }

    /**
     * Smart, mobile optimized layout, that aligns items horizontally if there is space
     * or goes to the next line.
     * Items will have at least 400px width, or go beyond that (since flex-grow is set to 1).
     * If the device width is smaller than 400px, the width is set to the device width.
     */
    public SmartLayout() {
        this(true, "400px");
    }

    /**
     * Smart, mobile optimized layout, that aligns items horizontally if there is space
     * or goes to the next line.
     *
     * @param childGrow     if true child components will try to fill out the complete available space.
     * @param minChildWidth the min width of a child component.
     */
    public SmartLayout(boolean childGrow, String minChildWidth) {
        super(NoValue.GET);
        addClass("smart-layout");
        wrap(true);
        rsty("flex-direction");
        childGap(true);
        Consumer<AddedChildEvent> superAdd = _add;
        _add = (e) -> {
            if (childGrow) {
                e.childComp.grow(1);
            }
            e.childComp.sty("min-width", minChildWidth);
            superAdd.accept(e);
        };
    }
}
