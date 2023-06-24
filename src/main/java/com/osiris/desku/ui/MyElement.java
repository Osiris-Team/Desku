package com.osiris.desku.ui;

import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

public class MyElement extends Element {
    /**
     * Reference to component of this element.
     */
    public final Component<?> comp;
    public MyElement(Component<?> comp, String tag) {
        super(tag);
        this.comp = comp;
    }

    public MyElement(Component<?> comp,Tag tag, String baseUri, Attributes attributes) {
        super(tag, baseUri, attributes);
        this.comp = comp;
    }

    public MyElement(Component<?> comp,Tag tag, String baseUri) {
        super(tag, baseUri);
        this.comp = comp;
    }
}
