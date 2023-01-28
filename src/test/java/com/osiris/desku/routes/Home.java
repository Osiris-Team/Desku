package com.osiris.desku.routes;

import com.osiris.desku.Route;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class Home extends Route {
    public Home() {
        super("/");
    }

    @Override
    public Node loadContent() {
        Element l = new Element("div");
        l.attr("style", "width: 100%; height: 100%; background: red;");
        l.appendChild(new TextNode("Currently at "+ path));
        return l;
    }
}
