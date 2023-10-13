package com.osiris.desku.ui.css;

public class Theme extends CSS {

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
    // Colors
    public Attribute colorPrimary = new Attribute("--color-primary", "#1a81fa");
    public Attribute colorPrimay50 = new Attribute("--color-primary-50", "#1a81fa80");
    public Attribute colorPrimary10 = new Attribute("--color-primary-10", "#1a81fa1a");
    public Attribute colorText = new Attribute("--color-text", "#308fff");
    public Attribute colorBase = new Attribute("--color-base", "#f5f5f5");
    public Attribute colorContrast = new Attribute("--color-contrast", "#ffffff");
    // Icons
    public Attribute iconWidth = new Attribute("--icon-width", "16px");
    public Attribute iconHeight = new Attribute("--icon-height", "16px");

    public Scrollbar scrollbar = new Scrollbar();

    public Theme() {
        super("html");
    }

    @Override
    public String toCSS() {
        return super.toCSS() +
                ".icon{" +
                "width: " + iconWidth.getValue() + ";" +
                "height: " + iconHeight.getValue() + ";" +
                "}\n" + scrollbar.toCSS();
    }
}
