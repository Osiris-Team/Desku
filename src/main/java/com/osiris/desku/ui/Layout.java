package com.osiris.desku.ui;

public class Layout extends Component<Layout> {
    private String nextWidth, nextHeight;

    public Layout() {
        this(false);
    }

    public Layout(boolean isHorizontal) {
        this(isHorizontal, (Component<?>[]) null);
    }

    public Layout(Component<?>... children) {
        this(false, children);
    }

    public Layout(boolean isHorizontal, Component<?>... children) {
        init(this, "ly");
        if (children != null) {
            add(children);
        }
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
    public Layout add(Component<?>... components) {
        for (Component<?> c : components) {
            super.add(c);
            setCompSize(c);
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

    public Layout text(String s) {
        return textM(s);
    }

    public Layout textXS(String s) {
        Text t = new Text(s);
        t.sizeXS();
        add(t);
        setCompSize(t);
        return this;
    }

    public Layout textS(String s) {
        Text t = new Text(s);
        t.sizeS();
        add(t);
        setCompSize(t);
        return this;
    }

    public Layout textM(String s) {
        Text t = new Text(s);
        t.sizeM();
        add(t);
        setCompSize(t);
        return this;
    }

    public Layout textL(String s) {
        Text t = new Text(s);
        t.sizeL();
        add(t);
        return this;
    }

    public Layout textXL(String s) {
        Text t = new Text(s);
        t.sizeXL();
        add(t);
        setCompSize(t);
        return this;
    }

}
