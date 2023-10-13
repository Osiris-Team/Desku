package com.osiris.desku.ui.css;

public class Scrollbar extends CSS {

    public Attribute width = new Attribute("width", "0.5rem");
    public Attribute height = new Attribute("height", "0.5rem");
    public Attribute backgroundClip = new Attribute("background-clip", "padding-box");
    public Attribute border = new Attribute("border", "0.1rem solid transparent");
    public Attribute color = new Attribute("color", "rgba(0, 0, 0, 0.3)");
    public Thumb thumb = new Thumb();

    public Scrollbar() {
        super("*::-webkit-scrollbar,\n" +
                "*::-webkit-scrollbar-thumb");
    }

    @Override
    public String toCSS() {
        return super.toCSS() + thumb.toCSS();
    }

    public class Thumb extends CSS {

        public Attribute boxShadow = new Attribute("box-shadow", "inset 0 0 0 10px");

        public Thumb() {
            super("*::-webkit-scrollbar-thumb");
        }
    }
}
