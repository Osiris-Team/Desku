package com.osiris.desku.ui.css;

import java.util.ArrayList;
import java.util.List;

public class CSS {
    /**
     * https://developer.mozilla.org/en-US/docs/Web/CSS/CSS_Selectors
     */
    public String selector;
    public List<Theme.Attribute> attributes = new ArrayList<>();

    public CSS(String selector) {
        this.selector = selector;
    }

    /**
     * Replaces all hyphens by their uppercase version
     * of their next char.
     */
    public static String getJSCompatibleCSSKey(String s) {
        int indexOfHyphen = 0;
        while (true) {
            indexOfHyphen = s.indexOf("-");
            if (indexOfHyphen == -1) break;
            String c = String.valueOf(s.charAt(indexOfHyphen + 1));
            s = s.replaceAll("-" + c, c.toUpperCase());
        }
        return s;
    }

    public String toCSS() {
        StringBuilder sb = new StringBuilder();
        sb.append(selector + "{\n");
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

        public String toCSS() {
            return getKey() + ": " + getValue() + "; ";
        }
    }
}
