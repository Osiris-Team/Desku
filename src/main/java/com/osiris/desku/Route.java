package com.osiris.desku;

import com.osiris.desku.ui.Component;
import com.osiris.jlib.logger.AL;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

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

    public Document load() {
        Document html = Jsoup.parse("<!DOCTYPE html>\n" +
                "<!--\n" +
                "This file is auto-generated by Vaadin.\n" +
                "-->\n" +
                "\n" +
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
        Element outlet = html.getElementById("outlet");
        Component<?> content = loadContent();
        content.updateAll();
        outlet.appendChild(content.element);
        return html;
    }

    /**
     * Writes the in memory html, to an actual html file in the temp folder of the current user. <br>
     * If the path for this route is "/persons/john" for example then the html file will be created at: <br>
     * {@link App#tempDir}/{@link #hashCode()}/persons/john.html <br>
     * Note that the hash code is in hex format.
     *
     * @return the generated html file.
     */
    public File writeToTempFile() throws IOException {
        File file = new File(App.tempDir + "/" + Integer.toHexString(hashCode())
                + (path.equals("/") ? "/root.html" : (path + ".html")));
        Document html = load();

        // Create symbolic link in current folder to global-styles.css
        // Symbolic links can't be created in temp folder?
        File link = new File(file.getParentFile() + "/" + App.styles.getName());
        AL.info("Copy of global styles: " + link);
        synchronized (App.styles) {
            if (!link.exists()) {
                link.getParentFile().mkdirs();
                link.createNewFile();
            }
            Files.copy(App.styles.toPath(), link.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        Element elLink = new Element("link");
        elLink.attr("rel", "stylesheet");
        elLink.attr("href", App.styles.getName());
        html.getElementsByTag("head").get(0).appendChild(elLink);

        // Write html to temp file
        AL.info("Generate: " + file);
        file.getParentFile().mkdirs();
        if (!file.exists()) file.createNewFile();
        Files.write(file.toPath(), html.outerHtml().getBytes(StandardCharsets.UTF_8));
        return file;
    }

}
