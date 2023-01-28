package com.osiris.desku.routes;

import com.osiris.desku.Route;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

public class About extends Route {
    public About() {
        super("/about");
    }
    @Override
    public Node loadContent() {
        return new TextNode("Currently at "+ path);
    }
}
