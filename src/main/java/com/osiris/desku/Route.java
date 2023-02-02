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

    public UI createUI() {
        return new UI(this);
    }

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
                "    html{\n" +
                "      --space-xs: 0.25rem;\n" +
                "      --space-s: 0.5rem;\n" +
                "      --space-m: 1rem;\n" +
                "      --space-l: 1.5rem;\n" +
                "      --space-xl: 2.5rem;\n" +
                "      --font-size-xxs: 0.75rem;\n" +
                "      --font-size-xs: 0.8125rem;\n" +
                "      --font-size-s: 0.875rem;\n" +
                "      --font-size-m: 1rem;\n" +
                "      --font-size-l: 1.125rem;\n" +
                "      --font-size-xl: 1.375rem;\n" +
                "      --font-size-xxl: 1.75rem;\n" +
                "      --font-size-xxxl: 2.5rem;" +
                "    }\n" +
                "    #outlet * {\n" + // All children of outlet will be flex
                "      display: flex;\n" +
                "    }\n" +
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
