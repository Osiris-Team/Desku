package com.osiris.desku.cssbox;

/**
 * BoxBrowser.java
 * Copyright (c) 2005-2014 Radek Burget
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

import java.awt.*;
import java.awt.image.DirectColorModel;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.fit.cssbox.awt.BrowserCanvas;
import org.fit.cssbox.css.BackgroundDecoder;
import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.CSSUnits;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BlockBox;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.BrowserConfig;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.Inline;
import org.fit.cssbox.layout.InlineElement;
import org.fit.cssbox.layout.Rectangle;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.Viewport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;

import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.MediaSpec;
import cz.vutbr.web.css.NodeData;

import javax.swing.event.TreeSelectionListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;

/**
 * This demo implements a browser that displays the rendered box tree and the
 * corresponding page. Each box corresponds to an HTML element or a text node.
 *
 * @author burgetr
 */
public class BoxBrowser
{
    public int width = 500, height = 500;
    public int widthScreen = GraphicsEnvironment.isHeadless() ? 1920 :
            Toolkit.getDefaultToolkit().getScreenSize().width,
            heightScreen = GraphicsEnvironment.isHeadless() ? 1080 :
                    Toolkit.getDefaultToolkit().getScreenSize().height;
    public ColorModel colorModel = GraphicsEnvironment.isHeadless() ? new DirectColorModel(24, 0xff0000, 0xff00, 0xff, 0)
            : Toolkit.getDefaultToolkit().getColorModel();
    private int resolutionScreen = GraphicsEnvironment.isHeadless() ? 96
        : Toolkit.getDefaultToolkit().getScreenResolution();

    protected DefaultMutableTreeNode root;
    protected DefaultMutableTreeNode domRoot;
    protected BrowserConfig config;
    public static org.fit.cssbox.demo.BoxBrowser browser;

    protected JFrame mainWindow = null;  //  @jve:decl-index=0:visual-constraint="67,17"
    protected JPanel mainPanel = null;
    protected JPanel urlPanel = null;
    protected JPanel contentPanel = null;
    protected JPanel structurePanel = null;
    protected JPanel statusPanel = null;
    protected JTextField statusText = null;
    protected JLabel jLabel = null;
    protected JTextField urlText = null;
    protected JButton okButton = null;
    protected JScrollPane contentScroll = null;
    protected JPanel contentCanvas = null;
    protected JSplitPane mainSplitter = null;
    protected JToolBar showToolBar = null;
    protected JButton redrawButton = null;
    protected JPanel toolPanel = null;
    protected JScrollPane boxScroll = null;
    protected JTree boxTree = null;
    protected JSplitPane infoSplitter = null;
    protected JPanel infoPanel = null;
    protected JScrollPane infoScroll = null;
    protected JTable infoTable = null;
    protected JTabbedPane treeTabs = null;
    protected JPanel DOMPanel = null;
    protected JScrollPane domScroll = null;
    protected JTree domTree = null;
    public JList<StyleListItem> styleList;
    public JScrollPane styleScroll;


    public BoxBrowser()
    {
        this.config = new BrowserConfig();
        getUrlText();
    }

    public BrowserConfig getConfig()
    {
        return config;
    }

    /**
     * Reads the document, creates the layout and displays it
     * @param urlstring The URL of the document to display.
     * @return The final URL of the displayed document or <code>null</code> when the document couldn't be displayed.
     */
    public URL displayURL(String urlstring)
    {
        try {
            if (!urlstring.startsWith("http:") &&
                    !urlstring.startsWith("https:") &&
                    !urlstring.startsWith("ftp:") &&
                    !urlstring.startsWith("file:"))
                urlstring = "http://" + urlstring;

            DocumentSource docSource = new DefaultDocumentSource(urlstring);

            DOMSource parser = new DefaultDOMSource(docSource);
            Document doc = parser.parse();
            String encoding = parser.getCharset();

            MediaSpec media = new MediaSpec("screen");
            updateCurrentMedia(media);

            DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
            if (encoding == null)
                encoding = da.getCharacterEncoding();
            da.setDefaultEncoding(encoding);
            da.setMediaSpec(media);
            da.attributesToStyles();
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.addStyleSheet(null, CSSNorm.formsStyleSheet(), DOMAnalyzer.Origin.AGENT);
            da.getStyleSheets();

            contentCanvas = new BrowserCanvas(da.getRoot(), da, docSource.getURL());
            contentCanvas.setSize(width, height);
            contentScroll.setSize(width, height);
            ((BrowserCanvas) contentCanvas).setConfig(config);
            ((BrowserCanvas) contentCanvas).createLayout(contentScroll.getSize(), contentScroll.getVisibleRect());

            docSource.close();

            contentCanvas.addMouseListener(new MouseListener() {
                public void mouseClicked(MouseEvent e)
                {
                    System.out.println("Click: " + e.getX() + ":" + e.getY());
                    canvasClick(e.getX(), e.getY());
                }
                public void mousePressed(MouseEvent e) { }
                public void mouseReleased(MouseEvent e) { }
                public void mouseEntered(MouseEvent e) { }
                public void mouseExited(MouseEvent e) { }
            });
            contentScroll.setViewportView(contentCanvas);

            //=============================================================================
            return docSource.getURL();

        } catch (Exception e) {
            System.err.println("*** Error: "+e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Updates the given media specification according to the real screen parametres (if they may be obtained).
     * @param media The media specification to be updated.
     */
    protected void updateCurrentMedia(MediaSpec media)
    {
        Dimension size = getContentScroll().getViewport().getSize();
        ColorModel colors = colorModel;

        media.setDimensions(size.width, size.height);
        media.setDeviceDimensions(widthScreen, heightScreen);
        media.setColor(colors.getComponentSize()[0]);
        if (colors instanceof IndexColorModel)
        {
            media.setColorIndex(((IndexColorModel) colors).getMapSize());
        }
        media.setResolution(resolutionScreen);
    }

    /**
     * Recursively creates a tree from the box tree
     */
    protected DefaultMutableTreeNode createBoxTree(Box root)
    {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(root);
        if (root instanceof ElementBox)
        {
            ElementBox el = (ElementBox) root;
            for (int i = el.getStartChild(); i < el.getEndChild(); i++)
            {
                ret.add(createBoxTree(el.getSubBox(i)));
            }
        }
        return ret;
    }

    /**
     * Recursively creates a tree from the dom tree
     */
    protected DefaultMutableTreeNode createDomTree(Node root)
    {
        DefaultMutableTreeNode ret = new DefaultMutableTreeNode(root) {
            public static final long serialVersionUID = 1L;

            @Override
            public String toString()
            {
                Object o = getUserObject();
                if (o instanceof Element)
                {
                    Element el = (Element) getUserObject();
                    String ret = "<" + el.getNodeName();
                    NamedNodeMap attrs = el.getAttributes();
                    for (int i = 0; i < attrs.getLength(); i++)
                        ret += " " + attrs.item(i).getNodeName() + "=\"" + attrs.item(i).getNodeValue() + "\"";
                    ret += ">";
                    return ret;
                }
                else if (o instanceof Text)
                    return ((Text) o).getNodeValue();
                else
                    return super.toString();
            }
        };
        NodeList child = root.getChildNodes();
        for (int i = 0; i < child.getLength(); i++)
            ret.add(createDomTree(child.item(i)));
        return ret;
    }

    /**
     * Locates a box from its position
     */
    public DefaultMutableTreeNode locateBox(DefaultMutableTreeNode root, int x, int y)
    {
        DefaultMutableTreeNode found = null;
        Box box = (Box) root.getUserObject();
        Rectangle bounds = box.getAbsoluteBounds();
        if (bounds.contains(x, y))
            found = root;

        //find if there is something smallest that fits among the child boxes
        for (int i = 0; i < root.getChildCount(); i++)
        {
            DefaultMutableTreeNode inside = locateBox((DefaultMutableTreeNode) root.getChildAt(i), x, y);
            if (inside != null)
            {
                if (found == null)
                    found = inside;
                else
                {
                    Box fbox = (Box) found.getUserObject();
                    Box ibox = (Box) inside.getUserObject();
                    if (ibox.getAbsoluteBounds().width * ibox.getAbsoluteBounds().height <
                            fbox.getAbsoluteBounds().width * fbox.getAbsoluteBounds().height)
                        found = inside;
                }
            }
        }

        return found;
    }

    /**
     * Locates a DOM node in the DOM tree
     */
    public DefaultMutableTreeNode locateObjectInTree(DefaultMutableTreeNode root, Object obj)
    {
        if (root.getUserObject().equals(obj))
            return root;
        else
        {
            for (int i = 0; i < root.getChildCount(); i++)
            {
                DefaultMutableTreeNode ret = locateObjectInTree((DefaultMutableTreeNode) root.getChildAt(i), obj);
                if (ret != null)
                    return ret;
            }
            return null;
        }
    }

    /**
     * This is called when the browser canvas is clicked
     */
    public void canvasClick(int x, int y)
    {
        DefaultMutableTreeNode node = locateBox(root, x, y);
        if (node != null)
        {
            TreePath select = new TreePath(node.getPath());
            boxTree.setSelectionPath(select);
            boxTree.expandPath(select);
            boxTree.scrollPathToVisible(select);

            if (node.getUserObject() instanceof Box)
            {
                Node domNode = ((Box) node.getUserObject()).getNode();
                if (domNode != null)
                {
                    DefaultMutableTreeNode dt = locateObjectInTree(domRoot, domNode);
                    if (dt != null)
                    {
                        TreePath dselect = new TreePath(dt.getPath());
                        domTree.setSelectionPath(dselect);
                        domTree.expandPath(dselect);
                        domTree.scrollPathToVisible(dselect);
                    }
                }
            }
        }
    }

    public void displayBoxInfo(Box box)
    {
        Vector<String> cols = infoTableData("Property", "Value");

        Vector<Vector <String>> vals = new Vector<Vector <String>>();
        vals.add(infoTableData("ID", box.getOrder() + " (" + box.getSplitId() + ")"));
        vals.add(infoTableData("Parent", (box.getParent() == null) ? "- none -" : box.getParent().toString()));
        vals.add(infoTableData("Cont. block box", (box.getContainingBlockBox() == null) ? "- none -" : box.getContainingBlockBox().toString()));
        vals.add(infoTableData("Clip. block", (box.getClipBlock() == null) ? "- none -" : box.getClipBlock().toString()));
        vals.add(infoTableData("S. parent", (box.getStackingParent() == null) ? "- none -" : box.getStackingParent().toString()));
        vals.add(infoTableData("Class", box.getClass().getSimpleName()));
        vals.add(infoTableData("Displayed", "" + box.isDisplayed()));
        vals.add(infoTableData("Visible", "" + box.isVisible()));
        vals.add(infoTableData("Empty", "" + box.isEmpty()));
        vals.add(infoTableData("Whitespace", "" + box.isWhitespace()));
        vals.add(infoTableData("Bounds", boundString(box.getBounds())));
        vals.add(infoTableData("AbsBounds", boundString(box.getAbsoluteBounds())));
        vals.add(infoTableData("Content", boundString(box.getContentBounds())));
        vals.add(infoTableData("Color", box.getVisualContext().getColor().toString()));
        vals.add(infoTableData("Font name", box.getVisualContext().getFontInfo().getFamily()));
        //vals.add(infoTableData("Font size", box.getVisualContext().getFont().getSize() + "pt (" + CSSUnits.pixels(box.getVisualContext().getFont().getSize()) + "px)"));
        vals.add(infoTableData("Font size", box.getVisualContext().getFontSize() + "pt (" + CSSUnits.pixels(box.getVisualContext().getFontSize()) + "px)"));
        vals.add(infoTableData("Text decor", box.getVisualContext().getTextDecorationString()));
        vals.add(infoTableData("Min width", String.valueOf(box.getMinimalWidth())));
        vals.add(infoTableData("Max width", String.valueOf(box.getMaximalWidth())));

        if (box instanceof ElementBox)
        {
            ElementBox eb = (ElementBox) box;
            BackgroundDecoder bg = new BackgroundDecoder(eb);
            vals.add(infoTableData("Display", eb.getDisplayString()));
            vals.add(infoTableData("BgColor", (bg.getBgcolor() == null) ? "" : bg.getBgcolor().toString()));
            vals.add(infoTableData("Margin", eb.getMargin().toString()));
            vals.add(infoTableData("EMargin", eb.getEMargin().toString()));
            vals.add(infoTableData("Padding", eb.getPadding().toString()));
            vals.add(infoTableData("Border", eb.getBorder().toString()));
            vals.add(infoTableData("LineH", String.valueOf(eb.getLineHeight()) + "px"));
            vals.add(infoTableData("Position", eb.getPositionString()));
            vals.add(infoTableData("Coords", eb.getCoords().toString()));
            vals.add(infoTableData("ZIndex", (eb.hasZIndex()) ? String.valueOf(eb.getZIndex()) : "(auto)"));
        }

        if (box instanceof Inline)
        {
            Inline ib = (Inline) box;
            vals.add(infoTableData("MaxLineH", String.valueOf(ib.getMaxLineHeight())));
            vals.add(infoTableData("TotalLine", String.valueOf(ib.getTotalLineHeight())));
            vals.add(infoTableData("Baseline", String.valueOf(ib.getBaselineOffset())));
            vals.add(infoTableData("Below base", String.valueOf(ib.getBelowBaseline())));
            vals.add(infoTableData("Half Lead", String.valueOf(ib.getHalfLead())));
        }

        if (box instanceof InlineElement)
        {
            InlineElement ib = (InlineElement) box;
            vals.add(infoTableData("LineboxOfs", String.valueOf(ib.getLineboxOffset()) + "px"));
            vals.add(infoTableData("VAlign", ib.getVerticalAlign().toString()));
        }

        if (box instanceof BlockBox)
        {
            BlockBox eb = (BlockBox) box;
            vals.add(infoTableData("Content layout", (eb.containsBlocks() ? "blocks" : "inline")));
            vals.add(infoTableData("Float", eb.getFloatingString()));
            vals.add(infoTableData("Overflow-X", eb.getOverflowXString()));
            vals.add(infoTableData("Overflow-Y", eb.getOverflowYString()));
            vals.add(infoTableData("Clear", eb.getClearingString()));
            vals.add(infoTableData("Reference", (eb.getAbsReference() == null) ? "- none -" : eb.getAbsReference().toString()));
            vals.add(infoTableData("DOM parent", (eb.getDomParent() == null) ? "- none -" : eb.getDomParent().toString()));
            vals.add(infoTableData("floatY", String.valueOf(eb.getFloatY())));
            vals.add(infoTableData("Indent", String.valueOf(eb.getIndent())));
        }

        DefaultTableModel tab = new DefaultTableModel(vals, cols);
        infoTable.setModel(tab);

        if (box instanceof ElementBox)
        {
            NodeData style = ((ElementBox) box).getStyle();
            List<String> names = new ArrayList<String>(style.getPropertyNames());
            Collections.sort(names);
            StyleListItem[] items = new StyleListItem[names.size()];
            int i = 0;
            for (String name : names)
            {
                String text = name + ": " + style.getAsString(name, true);
                Declaration d = style.getSourceDeclaration(name);
                String srcd = (d == null) ? "???" : d.toString();
                String src = (d != null && d.getSource() != null) ? d.getSource().toString() : "<unknown>";
                items[i++] = new StyleListItem(text, src + " - " + srcd);
            }
            styleList.setListData(items);
        }
    }

    class StyleListItem
    {
        public String text;
        public String tooltip;

        public StyleListItem(String text, String tooltip)
        {
            this.text = text;
            this.tooltip = tooltip;
        }

        public String getToolTipText()
        {
            return tooltip;
        }

        public String toString()
        {
            return text;
        }
    }

    public Vector<String> infoTableData(String prop, String value)
    {
        Vector<String> cols = new Vector<String>(2);
        cols.add(prop);
        cols.add(value);
        return cols;
    }

    public String boundString(Rectangle rect)
    {
        return "[" + rect.x + ", "
                + rect.y + ", "
                + rect.width + ", "
                + rect.height + "]";
    }

    public void drawExtent(Box box, Graphics2D g)
    {
        if (box instanceof ElementBox)
        {
            final ElementBox eb = (ElementBox) box;
            //draw the full box
            g.setColor(Color.RED);
            g.draw(awtRect2D(eb.getAbsoluteBounds()));

            //draw the content box
            g.setColor(Color.ORANGE);
            g.draw(awtRect2D(eb.getAbsoluteContentBounds()));

            //draw the real content box
            /*g.setColor(Color.GREEN);
            Rectangle r = getMinimalBounds();
            g.drawRect(r.x, r.y, r.width, r.height);*/
        }
        else if (box instanceof TextBox)
        {
            final TextBox tb = (TextBox) box;
            //draw the full box
            g.setColor(Color.ORANGE);
            g.draw(awtRect2D(tb.getAbsoluteBounds()));
            /*for (int i = 0; i < getText().length(); i++)
            {
                if (i != 0) System.out.print(" : ");
                char ch = getText().charAt(i);
                System.out.print(ch);
                System.out.print((int) ch);
                if (Character.isWhitespace(ch))
                    System.out.print("!");
                if (Character.isSpaceChar(ch))
                    System.out.print("*");
            }
            System.out.println();*/

            g.setColor(Color.MAGENTA);
            float y = tb.getAbsoluteContentY();
            float h = tb.getTotalLineHeight();
            g.draw(new Rectangle2D.Float(tb.getAbsoluteContentX(), y, tb.getContentWidth(), h));

            g.setColor(Color.BLUE);
            y = tb.getAbsoluteContentY() + tb.getBaselineOffset();
            g.draw(new Rectangle2D.Float(tb.getAbsoluteContentX(), y, tb.getContentWidth(), 1));
        }
    }

    public Rectangle2D awtRect2D(Rectangle rect)
    {
        if (rect == null)
            return null;
        return new Rectangle2D.Float(rect.x, rect.y, rect.width, rect.height);
    }

    public BrowserCanvas getBrowserCanvas()
    {
        return (BrowserCanvas) contentCanvas;
    }

    //===========================================================================

    /**
     * This method initializes jFrame
     *
     * @return javax.swing.JFrame
     */
    public JFrame getMainWindow()
    {
        if (mainWindow == null)
        {
            mainWindow = new JFrame();
            mainWindow.setTitle("Box Browser");
            mainWindow.setVisible(true);
            mainWindow.setBounds(new java.awt.Rectangle(0, 0, 583, 251));
            mainWindow.setContentPane(getMainPanel());
            mainWindow.addWindowListener(new java.awt.event.WindowAdapter()
            {
                public void windowClosing(java.awt.event.WindowEvent e)
                {
                    mainWindow.setVisible(false);
                    System.exit(0);
                }
            });
        }
        return mainWindow;
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    public JPanel getMainPanel()
    {
        if (mainPanel == null)
        {
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.gridy = -1;
            gridBagConstraints2.anchor = GridBagConstraints.WEST;
            gridBagConstraints2.gridx = -1;
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.fill = java.awt.GridBagConstraints.BOTH;
            gridBagConstraints11.weighty = 1.0;
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.weightx = 1.0;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.weightx = 1.0;
            gridBagConstraints3.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints3.gridwidth = 1;
            gridBagConstraints3.gridy = 3;
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.gridwidth = 1;
            gridBagConstraints.gridy = 1;
            mainPanel = new JPanel();
            mainPanel.setLayout(new GridBagLayout());
            mainPanel.add(getJPanel3(), gridBagConstraints2);
            mainPanel.add(getUrlPanel(), gridBagConstraints);
            mainPanel.add(getMainSplitter(), gridBagConstraints11);
            mainPanel.add(getStatusPanel(), gridBagConstraints3);
        }
        return mainPanel;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getUrlPanel()
    {
        if (urlPanel == null)
        {
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridy = 0;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.gridx = 1;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.gridx = 3;
            gridBagConstraints7.insets = new java.awt.Insets(4,0,5,7);
            gridBagConstraints7.gridy = 1;
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.weightx = 1.0;
            gridBagConstraints6.insets = new java.awt.Insets(0,5,0,5);
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.gridy = 1;
            gridBagConstraints5.anchor = java.awt.GridBagConstraints.CENTER;
            gridBagConstraints5.insets = new java.awt.Insets(0,6,0,0);
            gridBagConstraints5.gridx = 0;
            jLabel = new JLabel();
            jLabel.setText("Location :");
            urlPanel = new JPanel();
            urlPanel.setLayout(new GridBagLayout());
            urlPanel.add(jLabel, gridBagConstraints5);
            urlPanel.add(getUrlText(), gridBagConstraints6);
            urlPanel.add(getOkButton(), gridBagConstraints7);
        }
        return urlPanel;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getContentPanel()
    {
        if (contentPanel == null)
        {
            GridLayout gridLayout1 = new GridLayout();
            gridLayout1.setRows(1);
            contentPanel = new JPanel();
            contentPanel.setLayout(gridLayout1);
            contentPanel.add(getContentScroll(), null);
        }
        return contentPanel;
    }

    /**
     * This method initializes jPanel1
     *
     * @return javax.swing.JPanel
     */
    public JPanel getStructurePanel()
    {
        if (structurePanel == null)
        {
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            structurePanel = new JPanel();
            structurePanel.setPreferredSize(new java.awt.Dimension(200, 408));
            structurePanel.setLayout(gridLayout);
            structurePanel.add(getBoxScroll(), null);
        }
        return structurePanel;
    }

    /**
     * This method initializes jPanel2
     *
     * @return javax.swing.JPanel
     */
    public JPanel getStatusPanel()
    {
        if (statusPanel == null)
        {
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints4.weightx = 1.0;
            gridBagConstraints4.insets = new java.awt.Insets(0,7,0,0);
            gridBagConstraints4.gridy = 2;
            statusPanel = new JPanel();
            statusPanel.setLayout(new GridBagLayout());
            statusPanel.add(getStatusText(), gridBagConstraints4);
        }
        return statusPanel;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    public JTextField getStatusText()
    {
        if (statusText == null)
        {
            statusText = new JTextField();
            statusText.setEditable(false);
            statusText.setText("Browser ready.");
        }
        return statusText;
    }

    /**
     * This method initializes jTextField
     *
     * @return javax.swing.JTextField
     */
    public JTextField getUrlText()
    {
        if (urlText == null)
        {
            urlText = new JTextField();
            urlText.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    displayURL(urlText.getText());
                }
            });
        }
        return urlText;
    }

    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    public JButton getOkButton()
    {
        if (okButton == null)
        {
            okButton = new JButton();
            okButton.setText("Go!");
            okButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    displayURL(urlText.getText());
                }
            });
        }
        return okButton;
    }

    /**
     * This method initializes jScrollPane
     *
     * @return javax.swing.JScrollPane
     */
    public JScrollPane getContentScroll()
    {
        if (contentScroll == null)
        {
            contentScroll = new JScrollPane();
            contentScroll.setViewportView(getContentCanvas());
            contentScroll.addComponentListener(new java.awt.event.ComponentAdapter()
            {
                public void componentResized(java.awt.event.ComponentEvent e)
                {
                    if (contentCanvas != null && contentCanvas instanceof BrowserCanvas)
                    {
                        ((BrowserCanvas) contentCanvas).createLayout(contentScroll.getSize(), contentScroll.getViewport().getViewRect());
                        contentScroll.repaint();
                    }
                }
            });
            /*contentScroll.getViewport().addChangeListener(new javax.swing.event.ChangeListener()
            {
                public void stateChanged(ChangeEvent e)
                {
                    if (contentCanvas != null && contentCanvas instanceof BrowserCanvas)
                    {
                        ((BrowserCanvas) contentCanvas).updateVisibleArea(contentScroll.getViewport().getViewRect());
                        contentScroll.repaint();
                    }
                }
            });*/
        }
        return contentScroll;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getContentCanvas()
    {
        if (contentCanvas == null)
        {
            contentCanvas = new JPanel();
        }
        return contentCanvas;
    }

    /**
     * This method initializes jSplitPane
     *
     * @return javax.swing.JSplitPane
     */
    public JSplitPane getMainSplitter()
    {
        if (mainSplitter == null)
        {
            mainSplitter = new JSplitPane();
            mainSplitter.setLeftComponent(getTreeTabs());
            mainSplitter.setRightComponent(getInfoSplitter());
        }
        return mainSplitter;
    }

    /**
     * This method initializes jToolBar
     *
     * @return javax.swing.JToolBar
     */
    public JToolBar getShowToolBar()
    {
        if (showToolBar == null)
        {
            showToolBar = new JToolBar();
            showToolBar.add(getRedrawButton());
        }
        return showToolBar;
    }


    /**
     * This method initializes jButton
     *
     * @return javax.swing.JButton
     */
    public JButton getRedrawButton()
    {
        if (redrawButton == null)
        {
            redrawButton = new JButton();
            redrawButton.setText("Clear");
            redrawButton.setMnemonic(KeyEvent.VK_UNDEFINED);
            redrawButton.addActionListener(new java.awt.event.ActionListener()
            {
                public void actionPerformed(java.awt.event.ActionEvent e)
                {
                    ((BrowserCanvas) contentCanvas).redrawBoxes();
                    contentCanvas.repaint();
                }
            });
        }
        return redrawButton;
    }

    /**
     * This method initializes jPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getJPanel3()
    {
        if (toolPanel == null)
        {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.LEFT);
            toolPanel = new JPanel();
            toolPanel.setLayout(flowLayout);
            toolPanel.add(getShowToolBar(), null);
        }
        return toolPanel;
    }

    /**
     * This method initializes boxScroll
     *
     * @return javax.swing.JScrollPane
     */
    public JScrollPane getBoxScroll()
    {
        if (boxScroll == null)
        {
            boxScroll = new JScrollPane();
            boxScroll.setViewportView(getBoxTree());
        }
        return boxScroll;
    }

    /**
     * This method initializes boxTree
     *
     * @return javax.swing.JTree
     */
    public JTree getBoxTree()
    {
        if (boxTree == null)
        {
            boxTree = new JTree();
            boxTree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("(box tree)")));
            boxTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener()
            {
                public void valueChanged(javax.swing.event.TreeSelectionEvent e)
                {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) boxTree.getLastSelectedPathComponent();
                    if (node != null)
                    {
                        Box box = (Box) node.getUserObject();
                        if (box != null)
                        {
                            drawExtent(box, ((BrowserCanvas) contentCanvas).getEngine().getImageGraphics());
                            contentCanvas.repaint();
                            displayBoxInfo(box);

                            if (box.getNode() != null)
                            {
                                DefaultMutableTreeNode dt = locateObjectInTree(domRoot, box.getNode());
                                if (dt != null)
                                {
                                    TreePath dselect = new TreePath(dt.getPath());
                                    domTree.setSelectionPath(dselect);
                                    domTree.expandPath(dselect);
                                    domTree.scrollPathToVisible(dselect);
                                }
                            }
                        }
                    }
                }
            });
        }
        return boxTree;
    }

    /**
     * This method initializes infoSplitter
     *
     * @return javax.swing.JSplitPane
     */
    public JSplitPane getInfoSplitter()
    {
        if (infoSplitter == null)
        {
            infoSplitter = new JSplitPane();
            infoSplitter.setResizeWeight(1.0);
            infoSplitter.setDividerLocation(800);
            infoSplitter.setLeftComponent(getContentPanel());
            infoSplitter.setRightComponent(getInfoPanel());
        }
        return infoSplitter;
    }

    /**
     * This method initializes infoPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getInfoPanel()
    {
        if (infoPanel == null)
        {
            GridLayout gridLayout2 = new GridLayout();
            gridLayout2.setRows(2);
            gridLayout2.setColumns(1);
            infoPanel = new JPanel();
            infoPanel.setLayout(gridLayout2);
            infoPanel.add(getInfoScroll(), null);
            infoPanel.add(getStyleScroll());
        }
        return infoPanel;
    }

    /**
     * This method initializes infoScroll
     *
     * @return javax.swing.JScrollPane
     */
    public JScrollPane getInfoScroll()
    {
        if (infoScroll == null)
        {
            infoScroll = new JScrollPane();
            infoScroll.setViewportView(getInfoTable());
        }
        return infoScroll;
    }

    /**
     * This method initializes infoTable
     *
     * @return javax.swing.JTable
     */
    public JTable getInfoTable()
    {
        if (infoTable == null)
        {
            infoTable = new JTable();
        }
        return infoTable;
    }

    /**
     * This method initializes treeTabs
     *
     * @return javax.swing.JTabbedPane
     */
    public JTabbedPane getTreeTabs()
    {
        if (treeTabs == null)
        {
            treeTabs = new JTabbedPane();
            treeTabs.addTab("Box Tree", getStructurePanel());
            treeTabs.addTab("DOM Tree", null, getDOMPanel(), null);
        }
        return treeTabs;
    }

    public JList<StyleListItem> getStyleList() {
        if (styleList == null) {
            styleList = new JList<StyleListItem>() {
                public static final long serialVersionUID = 1L;
                public String getToolTipText(MouseEvent e) {
                    int index = locationToIndex(e.getPoint());
                    if (-1 < index) {
                        StyleListItem item = (StyleListItem) getModel().getElementAt(
                                index);
                        return item.getToolTipText();
                    } else {
                        //return super.getToolTipText();
                        return null;
                    }
                }
            };
        }
        return styleList;
    }

    public JScrollPane getStyleScroll() {
        if (styleScroll == null) {
            styleScroll = new JScrollPane();
            styleScroll.setViewportView(getStyleList());
        }
        return styleScroll;
    }

    /**
     * This method initializes DOMPanel
     *
     * @return javax.swing.JPanel
     */
    public JPanel getDOMPanel()
    {
        if (DOMPanel == null)
        {
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.fill = GridBagConstraints.BOTH;
            gridBagConstraints8.weighty = 1.0;
            gridBagConstraints8.weightx = 1.0;
            DOMPanel = new JPanel();
            DOMPanel.setLayout(new GridBagLayout());
            DOMPanel.add(getDomScroll(), gridBagConstraints8);
        }
        return DOMPanel;
    }

    /**
     * This method initializes domScroll
     *
     * @return javax.swing.JScrollPane
     */
    public JScrollPane getDomScroll()
    {
        if (domScroll == null)
        {
            domScroll = new JScrollPane();
            domScroll.setViewportView(getDomTree());
        }
        return domScroll;
    }

    /**
     * This method initializes domTree
     *
     * @return javax.swing.JTree
     */
    public JTree getDomTree()
    {
        if (domTree == null)
        {
            domTree = new JTree();
            domTree.addTreeSelectionListener(new TreeSelectionListener()
            {
                public void valueChanged(TreeSelectionEvent e)
                {
                    //DefaultMutableTreeNode node = (DefaultMutableTreeNode) domTree.getLastSelectedPathComponent();
                    /*if (node != null)
                    {
                        Box box = (Box) node.getUserObject();
                        if (box != null)
                        {
                            box.drawExtent(((BrowserCanvas) contentCanvas).getImageGraphics());
                            contentCanvas.repaint();
                            displayBoxInfo(box);
                        }
                    }*/
                }
            });
        }
        return domTree;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        browser = new org.fit.cssbox.demo.BoxBrowser();
        JFrame main = browser.getMainWindow();
        main.setSize(1200,600);
        main.setVisible(true);
    }
}

