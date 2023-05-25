package com.osiris.desku.ui.layout;

import com.osiris.desku.ui.Component;

public class Layout extends Component<Layout> {
    public Component<?> lastAdded;
    private String nextWidth, nextHeight;

    public Layout() {
        this(false);
    }

    // Do not accept children in constructor
    // to make sure that styling comes directly after the constructor call,
    // and not after adding children, to enhance code readability.
    // This for example is good:
    // layout().padding(true).add(child1, child2, etc...)
    // This not so much:
    // layout(child1, child2, etc...).padding(true)
    public Layout(boolean isHorizontal) {
        super("ly");
        align(isHorizontal);
    }

    private void setCompSize(Component comp) {
        if (nextWidth != null) {
            comp.width(nextWidth);
            nextWidth = null;
        }
        if (nextHeight != null) {
            comp.height(nextHeight);
            nextHeight = null;
        }
    }

    public Layout align(boolean horizontal) {
        if (horizontal) {
            childHorizontal();
        } else {
            childVertical();
        }
        return this;
    }

    /**
     * Same as {@link Component#add(Component[])},
     * but the first components' width/height will be
     * set if {@link #widthNext(String)} or {@link #heightNext(String)} was called before.
     */
    @Override
    public Layout add(Component<?>... comps) {
        for (Component<?> c : comps) {
            super.add(c);
            setCompSize(c);
            lastAdded = c;
        }
        return this;
    }

    /**
     * The width of the component that is added next.
     */
    public Layout widthNext(String s) {
        nextWidth = (s);
        return this;
    }

    /**
     * The height of the component that is added next.
     */
    public Layout heightNext(String s) {
        nextHeight = (s);
        return this;
    }

    /**
     * Creates, adds and returns a new child layout with vertical component alignment.
     */
    public Layout vertical() {
        Layout l = new Layout(false);
        add(l);
        setCompSize(l);
        return l;
    }

    /**
     * Creates, adds and returns a new child layout with horizontal component alignment.
     */
    public Layout horizontal() {
        Layout l = new Layout(true);
        add(l);
        setCompSize(l);
        return l;
    }

}
