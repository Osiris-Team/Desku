package com.osiris.desku.ui;

import java.util.ArrayList;
import java.util.List;

public class Theme {
    public List<Attribute> attributes = new ArrayList<>();
    // Space sizes
    public Attribute spaceXS = new Attribute("--space-xs", "0.25rem");
    public Attribute spaceS = new Attribute("--space-s", "0.5rem");
    public Attribute spaceM = new Attribute("--space-m", "1rem");
    public Attribute spaceL = new Attribute("--space-l", "1.5rem");
    public Attribute spaceXL = new Attribute("--space-xl", "2.5rem");
    // Font sizes
    public Attribute fontXXS = new Attribute("--font-size-xxs", "0.75rem");
    public Attribute fontXS = new Attribute("--font-size-xs", "0.8125rem");
    public Attribute fontS = new Attribute("--font-size-s", "0.875rem");
    public Attribute fontM = new Attribute("--font-size-m", "1rem");
    public Attribute fontL = new Attribute("--font-size-l", "1.125rem");
    public Attribute fontXL = new Attribute("--font-size-xl", "1.375rem");
    public Attribute fontXXL = new Attribute("--font-size-xxl", "1.75rem");
    public Attribute fontXXXL = new Attribute("--font-size-xxxl", "2.5rem");

    public String toCss() {
        StringBuilder sb = new StringBuilder();
        sb.append("html{\n");
        for (Attribute attr : attributes) {
            sb.append("  ").append(attr.getKey()).append(": ").append(attr.getValue()).append(";\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    public class Attribute extends org.jsoup.nodes.Attribute {
        public Attribute(String key, String value) {
            super(key, value);
            attributes.add(this);
        }
    }
}
