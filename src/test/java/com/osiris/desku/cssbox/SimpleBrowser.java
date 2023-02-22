package com.osiris.desku.cssbox;

/*
 * SimpleBrowser.java
 * Copyright (c) 2005-2007 Radek Burget
 *
 * CSSBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CSSBox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with CSSBox. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import org.apache.xerces.dom.AttrImpl;
import org.fit.cssbox.awt.BrowserCanvas;
import org.fit.cssbox.awt.GraphicsEngine;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.Dimension;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.*;

import javax.swing.*;


/**
 * An example of using CSSBox for the HTML page rendering and display.
 * It parses the style sheets and creates a box tree describing the
 * final layout. As the HTML parser, jTidy is used.
 *
 * @author  burgetr
 */
public class SimpleBrowser extends JPanel
{
    public static final long serialVersionUID = -1336331141597077348L;

    public GraphicsEngine engine;

    /** The swing canvas for displaying the rendered document */
    public javax.swing.JPanel browserCanvas;

    /** Scroll pane for the canvas */
    public javax.swing.JScrollPane documentScroll;

    /** Root DOM Element of the document body */
    public Element docroot;

    /** The CSS analyzer of the DOM tree */
    public DOMAnalyzer decoder;

    public SimpleBrowser(String url){
        try {
            //Open the network connection
            DocumentSource docSource = new DefaultDocumentSource(url);

            //Parse the input document
            DOMSource parser = new DefaultDOMSource(docSource);
            Document doc = parser.parse();

            //Create the CSS analyzer
            DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
            da.attributesToStyles(); //convert the HTML presentation attributes to inline styles
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT); //use the additional style sheet
            da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT); //render form fields using css
            da.getStyleSheets(); //load the author style sheets

            //Display the result
            setSize(500, 500);
            init(da.getRoot(), docSource.getURL(), da);

            docSource.close();

        } catch (Exception e) {
            System.out.println("Error: "+e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * Creates a new application window and displays the rendered document
     * @param root The root DOM element of the document body
     * @param baseurl The base URL of the document used for completing the relative paths
     * @param decoder The CSS analyzer that provides the effective style of the elements
     */
    public void init(Element root, URL baseurl, DOMAnalyzer decoder)
    {
        docroot = root;
        this.decoder = decoder;
        initComponents(baseurl);
    }

    /**
     * Creates and initializes the GUI components
     * @param baseurl The base URL of the document used for completing the relative paths
     */
    public void initComponents(URL baseurl)
    {
        documentScroll = new javax.swing.JScrollPane();

        //Create the browser canvas
        engine = new GraphicsEngine(docroot, decoder, baseurl);
        engine.setImage(new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB));
        engine.createLayout(new Dimension(getWidth(), getHeight()));
        browserCanvas = new BrowserCanvas(engine);

        //A simple mouse listener that displays the coordinates clicked
        browserCanvas.addMouseListener(new MouseListener() {
            public void mouseClicked(MouseEvent e)
            {
                System.out.println("Click: " + e.getX() + ":" + e.getY());
            }
            public void mousePressed(MouseEvent e) { }
            public void mouseReleased(MouseEvent e) { }
            public void mouseEntered(MouseEvent e) { }
            public void mouseExited(MouseEvent e) { }
        });

        setLayout(new java.awt.GridLayout(1, 0));

        //setTitle("CSSBox Browser");

        documentScroll.setViewportView(browserCanvas);
        add(documentScroll);
    }

    public List<Node> whereClassEquals(String className){
        List<Node> nodes = new ArrayList<>();
        loopRecursive(docroot, 0, (depth, node) -> {
            NamedNodeMap attr = node.getAttributes();
            if(attr != null)
                for (int i = 0; i < attr.getLength(); i++) {
                    String key = attr.item(i).getNodeName();
                    String value = attr.item(i).getNodeValue();
                    if(key.equals("class") && value.equals(className))
                        nodes.add(node);
                }
        });
        return nodes;
    }

    public void render(){
        Viewport viewport = engine.getViewport();
        viewport.doLayout(getWidth(), true, true); // canvasWidth is the new canvas width
        viewport.absolutePositions(); // re-compute absolute positions of sub-boxes
    }

    public void loopRecursive(Node el, int currentDepth, BiConsumer<Integer, Node> code){
        code.accept(currentDepth, el);
        NodeList childNodes = el.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            loopRecursive(childNodes.item(i), currentDepth + 1,  code);
        }
    }

    public void printContent(){
        printContent(System.out);
    }
    public void printContent(PrintStream out){
        loopRecursive(docroot, 0, (depth, node) -> {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < depth; i++) {
                sb.append("  ");
            }
            StringBuilder attributes = new StringBuilder();
            NamedNodeMap attributes1 = node.getAttributes();
            if(attributes1 != null)
                for (int i = 0; i < attributes1.getLength(); i++) {
                    attributes.append(attributes1.item(i).getNodeName())
                            .append("=\"").append(attributes1.item(i).getNodeValue()+"\"");
                }
            out.println(sb.toString() + node.getNodeName()+" "+attributes.toString());
        });
    }
}

