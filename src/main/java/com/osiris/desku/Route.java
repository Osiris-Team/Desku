package com.osiris.desku;

import com.osiris.desku.ui.Component;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public abstract class Route {
    /**
     * Must start with "/".
     */
    public String path;

    public Route(String path) {
        App.routes.add(this);
        this.path = path;
    }

    public abstract Component<?> loadContent();

    /**
     * @return the default HTML layout for this route. Content should be added to div with
     * the id "outlet".
     */
    public Document getDocument() {
        return Jsoup.parse("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "  <style>\n" +
                App.theme.toCss() +
                "    body, #outlet {\n" +
                "      height: 100vh;\n" +
                "      width: 100%;\n" +
                "      margin: 0;\n" +
                "    }\n" +
                "  </style>\n" +
                "  <!-- index.ts is included here automatically (either by the dev server or during the build) -->\n" +
                "</head>\n" +
                "<body>\n" +
                "  <!-- This outlet div is where the views are rendered -->\n" +
                "  <div id=\"outlet\"></div>\n" +
                "</body>\n" +
                "</html>");
    }

}
