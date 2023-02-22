package com.osiris.desku.cssbox;

import com.osiris.desku.OffscreenJFrame;
import org.apache.xerces.dom.AttrImpl;
import org.junit.jupiter.api.Test;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class CssBoxTest {
    @Test
    void test() throws IOException, InterruptedException {
        System.setProperty("java.awt.headless", "true");

        File testHtml = new File(System.getProperty("user.dir") + "/test.html");
        File testCss = new File(System.getProperty("user.dir") + "/test.css");
        testHtml.delete();
        testHtml.createNewFile();
        testCss.delete();
        testCss.createNewFile();
        Files.write(testHtml.toPath(), ("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\" />\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "  <style>\n" +
                //App.theme.toCss() +
                ".flex {\n" +
                "  display: flex;\n" +
                "  flex-wrap: wrap;\n" +
                "  justify-content: space-between;\n" +
                "}\n" +
                "\n" +
                ".item {\n" +
                "  width: 48%;\n" +
                "  height: 100px;\n" +
                "  margin-bottom: 2%;\n" +
                "}\n" +
                "\n" +
                ".item:nth-child(3n) {\n" +
                "  width: 100%;\n" +
                "}" +
                "div{" +
                "background: blue;" +
                "}"+
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
                "  <div id=\"outlet\">" +
                "<div style=\"width: 50%; height: 50%; background: red;\"></div>" +
                "<div class=\"container\">" +
                "<div class=\"item\" style=\"background: gray;\"></div>" +
                "<div class=\"item\" style=\"background: gray;\"></div>" +
                "<div class=\"item\" style=\"background: gray;\"></div>" +
                "<div class=\"item\" style=\"background: gray;\"></div>" +
                "<div class=\"item\" style=\"background: gray;\"></div>" +
                "</div>" +
                "</div>\n" +
                "</body>\n" +
                "</html>").getBytes(StandardCharsets.UTF_8));
        SimpleBrowser simpleBrowser = new SimpleBrowser("file:///" + testHtml.toString());
        ImageIO.write(simpleBrowser.engine.getImage(), "PNG", new File(System.getProperty("user.dir")+"/test.png"));
        simpleBrowser.whereClassEquals("container").get(0).getAttributes()
                .setNamedItem(new AttrImpl() {
                    @Override
                    public Document getOwnerDocument() {
                        return simpleBrowser.docroot.getOwnerDocument();
                    }

                    @Override
                    public String getNodeName() {
                        return "style";
                    }

                    @Override
                    public String getNodeValue() throws DOMException {
                        return "background: yellow;";
                    }
                });
        simpleBrowser.printContent();
        simpleBrowser.render();
        ImageIO.write(simpleBrowser.engine.getImage(), "PNG", new File(System.getProperty("user.dir")+"/test.png"));
        /*
        BoxBrowser browser = new BoxBrowser();
        browser.displayURL("file:///"+testHtml.toString());
        OffscreenJFrame f = new OffscreenJFrame(browser.contentScroll);
        f.startRender(img -> {}, 100);


        while (f.getLastRenderedImage() == null) Thread.yield();
        ImageIO.write(f.getLastRenderedImage(), "PNG", new File(System.getProperty("user.dir")+"/test.png"));
        */
        System.out.println("OK");
        while (true) Thread.sleep(100);

    }
}
