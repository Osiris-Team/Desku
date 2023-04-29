package com.osiris.desku.ui.display;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.jogamp.graph.font.FontFactory;
import com.osiris.desku.ui.Component;

public class Text extends Component<Text> {
    public Text(String s) {
        init(this, "txt");
        actor.appendText(s);
    }

    private void updateActor(){

    }

    public String get() {
        return actor.text();
    }

    public Text append(String s) {
        actor.appendText(s);
        return this;
    }

    public Text sizeXS() {
        style.put("font-size", "var(--font-size-xs)");
        return this;
    }

    public Text sizeS() {
        style.put("font-size", "var(--font-size-s)");
        return this;
    }

    public Text sizeM() {
        style.put("font-size", "var(--font-size-m)");
        return this;
    }

    public Text sizeL() {
        style.put("font-size", "var(--font-size-l)");
        return this;
    }

    public Text sizeXL() {
        style.put("font-size", "var(--font-size-xl)");
        return this;
    }

    public Text sizeXXL() {
        style.put("font-size", "var(--font-size-xxl)");
        return this;
    }

    public Text sizeXXXL() {
        style.put("font-size", "var(--font-size-xxxl)");
        return this;
    }

    public Text bold() {
        style.put("font-weight", "bold");
        return this;
    }

    public Text bolder() {
        style.put("font-weight", "bolder");
        return this;
    }
}
