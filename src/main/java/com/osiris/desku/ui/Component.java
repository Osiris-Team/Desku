package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ClickEvent;
import com.osiris.desku.ui.layout.Overlay;
import com.osiris.desku.utils.GodIterator;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import jdk.internal.net.http.common.Pair;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Component<T> {
    private static final AtomicInteger idCounter = new AtomicInteger();

    static {
        try {
            String styles = "#outlet * {\n" +
                    "  display: flex;\n" +  // All children of outlet will be flex
                    "}\n";
            App.appendToGlobalStyles(styles);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Equals the attribute "java-id" inside HTML and thus useful for finding this object via JavaScript. <br>
     * Example: The code below will return the object with the java-id = 5.
     * <pre>
     *     var element = document.querySelectorAll('[java-id="5"]')[0];
     * </pre>
     */
    public final int id = idCounter.getAndIncrement();
    public final CopyOnWriteArrayList<Component<?>> children = new CopyOnWriteArrayList<>();

    public static class AddedChildEvent {
        /**
         * Component that got added.
         */
        public final Component<?> childComp;
        /**
         * Should only be relevant and not null when either
         * {@link #isInsert} or {@link #isReplace} is true.
         */
        public final Component<?> otherChildComp;
        /**
         * If true, then {@link #otherChildComp} should now be the next child in the list after {@link #childComp}.
         */
        public final boolean isInsert;
        /**
         * If true, then {@link #otherChildComp} should NOT exist in the list anymore.
         */
        public final boolean isReplace;

        public AddedChildEvent(Component<?> childComp, Component<?> otherChildComp, boolean isInsert, boolean isReplace) {
            this.childComp = childComp;
            this.otherChildComp = otherChildComp;
            this.isInsert = isInsert;
            this.isReplace = isReplace;
        }
    }

    /**
     * Executed when a child was added on the Java side. <br>
     * It's a {@link  Pair} because it might be an insert operation and thus
     * the id of the component must be given.
     */
    public final Event<AddedChildEvent> onAddedChild = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Component<?>> onRemovedChild = new Event<>();
    /**
     * Executed when a style change was made on the Java side.
     */
    public final Event<Attribute> onStyleChanged = new Event<>();
    /**
     * Do not add actions via this variable, use {@link #onClick(Consumer)} instead.
     */
    public final Event<ClickEvent> _onClick = new Event<>();
    protected final ConcurrentHashMap<String, String> style = new ConcurrentHashMap<>();
    /**
     * The instance of the extending class. <br>
     * Is returned in pretty much all methods, to allow method chaining by returning
     * the extending class instead of {@link Component}.
     */
    public T target;
    /**
     * Jsoup {@link Element} that can be used to convert this
     * {@link Component} into an actual HTML string.
     * <u>
     * Note that changes to this element are only visible in the UI if
     * they are done before it gets loaded the first time. <br>
     * This means that changes to this element made in a click event for example
     * will not be visible in the UI. <br>
     * Use wrapper methods like {@link #innerHTML(String)} for example.
     * </u>
     */
    public Element element;

    public Component() {
        // Attach Java event listeners
        UI win = UI.get();
        Runnable registration = () -> {
            onAddedChild.addAction((e) -> { // event
                if (e.otherChildComp == null) {
                    children.add(e.childComp);
                    e.childComp.update();
                    element.appendChild(e.childComp.element);
                    win.browser.executeJavaScript(win.jsGetComp("parentComp", id) +
                                    "var tempDiv = document.createElement('div');\n" +
                                    "tempDiv.innerHTML = `" + e.childComp.element.outerHtml() + "`;\n" +
                                    "parentComp.appendChild(tempDiv.firstChild);\n",
                            "internal", 0);
                } else if (e.isInsert) {
                    int iOtherComp = children.indexOf(e.otherChildComp);
                    children.set(iOtherComp, e.childComp);
                    e.childComp.update();
                    element.insertChildren(iOtherComp, e.childComp.element);
                    win.browser.executeJavaScript(win.jsGetComp("parentComp", id) +
                                    win.jsGetComp("otherChildComp", e.otherChildComp.id) +
                                    "var tempDiv = document.createElement('div');\n" +
                                    "tempDiv.innerHTML = `" + e.childComp.element.outerHtml() + "`;\n" +
                                    "parentComp.insertBefore(tempDiv.firstChild, otherChildComp);\n",
                            "internal", 0);
                } else if (e.isReplace) {
                    int iOtherComp = children.indexOf(e.otherChildComp);
                    children.set(iOtherComp, e.childComp);
                    e.childComp.update();
                    element.insertChildren(iOtherComp, e.childComp.element);
                    win.browser.executeJavaScript(win.jsGetComp("parentComp", id) +
                                    win.jsGetComp("otherChildComp", e.otherChildComp.id) +
                                    "var tempDiv = document.createElement('div');\n" +
                                    "tempDiv.innerHTML = `" + e.childComp.element.outerHtml() + "`;\n" +
                                    "parentComp.insertBefore(tempDiv.firstChild, otherChildComp);\n",
                            "internal", 0);
                    onRemovedChild.execute(e.otherChildComp); // Removes from children, (node) children, and actual UI
                }
            });
            onRemovedChild.addAction((childComp) -> {
                children.remove(childComp);
                childComp.update();
                childComp.element.remove();
                win.browser.executeJavaScript(win.jsGetComp("comp", id) +
                                win.jsGetComp("childComp", childComp.id) +
                                "comp.removeChild(childComp);\n",
                        "internal", 0);
            });
            onStyleChanged.addAction((attribute) -> {
                if (attribute.getValue().isEmpty()) { // Remove style
                    String style = element.hasAttr("style") ? element.attributes().get("style") : "";
                    int iKeyFirstChar = style.indexOf(attribute.getKey());
                    if (iKeyFirstChar == -1) return; // Already doesn't exist, so no removal is needed
                    style = style.substring(0, iKeyFirstChar) + style.substring(style.indexOf(";", iKeyFirstChar) + 1);
                    element.attr("style", style); // Change in-memory representation
                    win.browser.executeJavaScript(win.jsGetComp("comp", id) + // Change UI representation
                                    "comp.style." + attribute.getKey() + " = ``;\n",
                            "internal", 0);
                } else { // Add style
                    String style = element.hasAttr("style") ? element.attributes().get("style") : "";
                    style += attribute.getKey() + ": " + attribute.getValue() + ";";
                    element.attr("style", style); // Change in-memory representation
                    win.browser.executeJavaScript(win.jsGetComp("comp", id) + // Change UI representation
                                    "comp.style." + attribute.getKey() + " = `" + attribute.getValue() + "`;\n",
                            "internal", 0);
                }
            });
        };
        if (!win.isLoading) registration.run();
        else win.onLoadStateChanged.addAction((action, event) -> {
            if (event.isLoading) return;
            action.remove();
            registration.run();
        }, AL::warn);
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, String tag) {
        this.target = target;
        this.element = new Element(tag);
        element.attr("java-id", String.valueOf(id));
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, Tag tag, String baseUri, Attributes attributes) {
        this.target = target;
        this.element = new Element(tag, baseUri, attributes);
        element.attr("java-id", String.valueOf(id));
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, Tag tag, String baseUri) {
        this.target = target;
        this.element = new Element(tag, baseUri);
        element.attr("java-id", String.valueOf(id));
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, Element element) {
        this.target = target;
        this.element = element;
        element.attr("java-id", String.valueOf(id));
    }

    /**
     * Executes the provided code synchronously in the current thread. <br>
     *
     * @param code the code to be executed now, contains this component as parameter.
     */
    public T now(Consumer<T> code) {
        code.accept(target);
        return target;
    }

    /**
     * Executes the provided code asynchronously in a new thread. <br>
     * This function needs to be run inside UI context
     * since it executes {@link UI#get()}, otherwise {@link NullPointerException} is thrown. <br>
     * <br>
     * Note that your code-block will have access to the current UI,
     * which means that you can add/remove/change UI components without issues.
     * This also means that you will have to handle Thread-safety yourself
     * when doing things to the same component from multiple threads at the same time.
     *
     * @param code the code to be executed asynchronously, contains this component as parameter.
     */
    public T later(Consumer<T> code) {
        UI ui = UI.get();
        Objects.requireNonNull(ui);
        Thread t = new Thread() {
            @Override
            public void run() {
                code.accept(target);
                UI.remove(this);
            }
        };
        UI.set(ui, t);
        t.start();
        return target;
    }

    /**
     * Same as {@link #later(Consumer)} but
     * adds an overlay that
     * shows the text "Loading..." and dims/darkens this component,
     * until the async task finishes.
     */
    public T laterWithOverlay(BiConsumer<T, Overlay> code) {
        later(_this -> {
            Overlay overlay = (Overlay) new Overlay(this)
                    .childCenter().putStyle("background", "rgba(0,0,0,0.3)")
                    .sizeFull();
            add(overlay);
            overlay.add(new Text("Loading...")
                    .putStyle("color", "white").sizeXL().selfCenter());
            overlay.add(new Text("This might take a while, please be patient.")
                    .putStyle("color", "white").sizeS().selfCenter());
            code.accept(target, overlay);
            remove(overlay);
        });
        return target;
    }

    public Consumer<AddedChildEvent> _add = (e) -> {
        if (e.otherChildComp == null) {
            children.add(e.childComp);
            onAddedChild.execute(e);
        } else if (e.isInsert) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            onAddedChild.execute(e);
        } else if (e.isReplace) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            onAddedChild.execute(e);
            onRemovedChild.execute(e.otherChildComp); // Removes from children, (node) children, and actual UI
        }
    };

    public T add(Iterable<Component<?>> comps) {
        if (comps == null) return target;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return target;
    }

    public T add(Component<?>... comps) {
        if (comps == null) return target;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return target;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public T addAt(int index, Component<?> comp) {
        if (comp == null) return target;
        Component<?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, true, false));
        return target;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public T replaceAt(int index, Component<?> comp) {
        if (comp == null) return target;
        Component<?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, false, true));
        return target;
    }

    /**
     * Does nothing if newComp or oldComp is null.
     *
     * @throws IndexOutOfBoundsException if oldComp does not exist in {@link #children}.
     */
    public T replace(Component<?> oldComp, Component<?> newComp) {
        if (oldComp == null || newComp == null) return target;
        if (!children.contains(oldComp))
            throw new IndexOutOfBoundsException("Provided old component to be replaced does not exist in children!");
        _add.accept(new AddedChildEvent(newComp, oldComp, false, true));
        return target;
    }

    public Consumer<Component<?>> _remove = child -> {
        if (children.contains(child)) {
            onRemovedChild.execute(child);
        }
    };

    public T removeAll() {
        for (Component<?> child : children) {
            _remove.accept(child);
        }
        return target;
    }

    public T remove(Component<?>... comps) {
        if (comps == null) return target;
        GodIterator.forEach(comps, _remove);
        return target;
    }

    public T remove(Iterable<Component<?>> comps) {
        if (comps == null) return target;
        GodIterator.forEach(comps, _remove);
        return target;
    }

    /**
     * Removes the element at the specified position in this list.
     * Shifts any subsequent elements to the left (subtracts one from their
     * indices).  Returns the element that was removed from the list.
     *
     * @throws IndexOutOfBoundsException {@inheritDoc}
     */
    public T removeAt(int index) {
        Component<?> child = children.get(index);
        _remove.accept(child);
        return target;
    }

    private static ArrayList<Component<?>> toList(Iterable<Component<?>> comps) {
        ArrayList<Component<?>> list = new ArrayList<>();
        for (Component<?> c : comps) {
            list.add(c);
        }
        return list;
    }

    public T putStyle(String key, String val) {
        style.put(key, val);
        onStyleChanged.execute(new Attribute(key, val));
        return target;
    }

    public T removeStyle(String key) {
        style.remove(key);
        onStyleChanged.execute(new Attribute(key, ""));
        return target;
    }

    /**
     * Must be called after changing the style or after adding/removing children to/from this component,
     * otherwise the changes won't be visible. <br>
     * If child components also changed call {@link #updateAll()} instead. <br>
     */
    public T update() {
        // Update element style
        StringBuilder sb = new StringBuilder();
        style.forEach((key, val) -> {
            sb.append(key).append(": ").append(val).append("; ");
        });
        element.attr("style", sb.toString());

        // Update element children list
        for (Element child : element.children()) { // Clear children
            child.remove();
        }
        for (Component<?> childComp : children) { // Set "new" children elements, from child components
            element.appendChild(childComp.element);
        }
        return target;
    }

    /**
     * Performs {@link #update()} for this and all child components recursively. <br>
     * Note that you don't have to call this function, since it already gets called before showing the window,
     * in {@link UI#getSnapshot()}.
     */
    public T updateAll() {
        // Update this style
        update();

        // Recursion for all children
        for (Component<?> childComp : children) {
            childComp.updateAll();
        }

        return target;
    }

    /**
     * Loops through all children recursively. <br>
     * Loop through {@link #children} instead if you only want the direct children of this component.
     */
    public void forEachChildRecursive(Consumer<Component<?>> code) {
        for (Component<?> child : this.children) {
            forEachChildRecursive(child, code);
        }
    }

    /**
     * Loops through all children recursively. <br>
     * Loop through {@link #children} instead if you only want the direct children of this component.
     */
    public void forEachChildRecursive(Component<?> comp, Consumer<Component<?>> code) {
        code.accept(comp);
        for (Component<?> child : comp.children) {
            forEachChildRecursive(child, code);
        }
    }

    /**
     * @see Element#text(String)
     */
    public T innerHTML(String text) {
        remove(children);
        add(new Text(text));
        return target;
    }

    /**
     * @see Element#html(String)
     */
    public T innerHTML(Component<?> comp) {
        remove(children);
        add(comp);
        return target;
    }

    public T sizeFull() {
        size("100%", "100%");
        return target;
    }

    /**
     * Sets width and height of the target component and return it for method chaining.
     */
    public T size(String width, String height) {
        putStyle("width", width);
        putStyle("height", height);
        return target;
    }

    /**
     * Sets width of the target component and return it for method chaining.
     */
    public T width(String s) {
        putStyle("width", s);
        return target;
    }

    /**
     * Sets height of the target component and return it for method chaining.
     */
    public T height(String s) {
        putStyle("height", s);
        return target;
    }


    public T padding(boolean b) {
        if (b) putStyle("padding", "var(--space-m)");
        else removeStyle("padding");
        return target;
    }

    public T padding(String s) {
        putStyle("padding", s);
        return target;
    }

    public T paddingLeft(String s) {
        putStyle("padding-left", s);
        return target;
    }

    public T paddingRight(String s) {
        putStyle("padding-right", s);
        return target;
    }

    public T paddingTop(String s) {
        putStyle("padding-top", s);
        return target;
    }

    public T paddingBottom(String s) {
        putStyle("padding-bottom", s);
        return target;
    }

    public T margin(boolean b) {
        if (b) putStyle("margin", "var(--space-m)");
        else removeStyle("margin");
        return target;
    }

    public T spacing(boolean b) {
        if (b) putStyle("spacing", "var(--space-m)");
        else removeStyle("spacing");
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T overflowDefault() {
        removeStyle("overflow");
        return target;
    }

    /**
     * By default, the overflow is visible, meaning that it is not clipped and it renders outside the element's box.
     */
    public T overflowVisible() {
        putStyle("overflow", "visible");
        return target;
    }

    /**
     * With the hidden value, the overflow is clipped, and the rest of the content is hidden.
     */
    public T overflowHidden() {
        putStyle("overflow", "hidden");
        return target;
    }

    /**
     * Setting the value to scroll, the overflow is clipped and a scrollbar is added to scroll inside the box.
     * Note that this will add a scrollbar both horizontally and vertically (even if you do not need it).
     * Thus {@link #overflowAuto()} might be better suited.
     */
    public T overflowScroll() {
        putStyle("overflow", "scroll");
        return target;
    }

    /**
     * The auto value is similar to scroll, but it adds scrollbars only when necessary.
     */
    public T overflowAuto() {
        putStyle("overflow", "auto");
        return target;
    }

    /**
     * By default, flex items are laid out in the source order. <br>
     * However, the order property controls the order in which they appear in the flex container. <br>
     * Items with the same order revert to source order. <br>
     * Default: 0 <br>
     */
    public T order(int i) {
        putStyle("order", String.valueOf(i));
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T orderDefault() {
        removeStyle("order");
        return target;
    }

    /**
     * This defines the ability for a flex item to grow if necessary.
     * It accepts a unitless value that serves as a proportion.
     * It dictates what amount of the available space inside the flex container the item should take up.
     * <p>
     * If all items have flex-grow set to 1, the remaining space
     * in the container will be distributed equally to all children.
     * If one of the children has a value of 2, that child would take up twice
     * as much of the space either one of the others (or it will try, at least). <br>
     * <p>
     * Negative numbers are invalid. <br>
     * Default: 0 <br>
     */
    public T grow(int i) {
        putStyle("grow", String.valueOf(i));
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T growDefault() {
        removeStyle("grow");
        return target;
    }


    /**
     * This defines the ability for a flex item to shrink if necessary. <br>
     * Negative numbers are invalid. <br>
     * Default: 1 <br>
     */
    public T shrink(int i) {
        putStyle("shrink", String.valueOf(i));
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T shrinkDefault() {
        removeStyle("shrink");
        return target;
    }

    /**
     * By default, flex items will all try to fit onto one line.
     * You can change that and allow the items to wrap as needed with this property. <br>
     * false/nowrap (default): all flex items will be on one line <br>
     * true/wrap: flex items will wrap onto multiple lines, from top to bottom. <br>
     */
    public T wrap(boolean b) {
        if (b) putStyle("flex-wrap", "wrap");
        else putStyle("flex-wrap", "nowrap");
        return target;
    }


    /**
     * align-self <br>
     * The element is positioned at the beginning of the container. <br>
     * This allows the default alignment (or the one specified by align-items)
     * to be overridden for individual flex items. <br>
     * Note that float, clear and vertical-align have no effect on a flex item.
     */
    public T selfStart() {
        putStyle("align-self", "flex-start");
        return target;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart()
     */
    public T selfEnd() {
        putStyle("align-self", "flex-end");
        return target;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart()
     */
    public T selfCenter() {
        putStyle("align-self", "center");
        return target;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart()
     */
    public T selfAuto() {
        putStyle("align-self", "auto");
        return target;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart()
     */
    public T selfStretch() {
        putStyle("align-self", "stretch");
        return target;
    }

    /**
     * Aligns items top to bottom.
     */
    public T childVertical() {
        putStyle("flex-direction", "column");
        return target;
    }

    /**
     * (Default) Aligns items left to right in ltr; right to left in rtl.
     */
    public T childHorizontal() {
        putStyle("flex-direction", "row");
        return target;
    }

    /**
     * justify-content <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public T childStart() {
        putStyle("justify-content", "flex-start");
        return target;
    }

    /**
     * justify-content <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public T childEnd() {
        putStyle("justify-content", "flex-end");
        return target;
    }

    /**
     * justify-content <br>
     * center: items are centered along the line
     */
    public T childCenter() {
        putStyle("justify-content", "center");
        return target;
    }

    /**
     * justify-content <br>
     * space-between: items are evenly distributed in the line; first item is on the start line, last item on the end line
     */
    public T childSpaceBetween() {
        putStyle("justify-content", "space-between");
        return target;
    }

    /**
     * justify-content <br>
     * space-around: items are evenly distributed in the line with equal space around them.
     * Note that visually the spaces aren't equal, since all the items
     * have equal space on both sides. The first item will have one unit of space
     * against the container edge, but two units of space between
     * the next item because that next item has its own spacing that applies.
     */
    public T childSpaceAround() {
        putStyle("justify-content", "space-around");
        return target;
    }

    /**
     * justify-content <br>
     * space-evenly: items are distributed so that the spacing
     * between any two items (and the space to the edges) is equal.
     */
    public T childSpaceEvenly() {
        putStyle("justify-content", "space-around");
        return target;
    }


    /**
     * align-items <br>
     * stretch: stretch to fill the container (still respect min-width/max-width)
     */
    public T childStretch() {
        putStyle("align-items", "stretch");
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public T childGap(String s) {
        putStyle("gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between rows/Y-Axis/height.
     */
    public T childGapY(String s) {
        putStyle("row-gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between columns/X-Axis/width.
     */
    public T childGapX(String s) {
        putStyle("column-gap", s);
        return target;
    }

    //
    // Listeners
    //

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, Consumer)
     */
    public T onClick(Consumer<ClickEvent<T>> code) {
        _onClick.addAction((event) -> code.accept(event));
        Component<T> _this = this;
        UI.get().registerJSListener("click", _this, (msg) -> {
            _onClick.execute(new ClickEvent<T>(msg, (T) _this)); // Executes all listeners
        });
        return target;
    }
}
