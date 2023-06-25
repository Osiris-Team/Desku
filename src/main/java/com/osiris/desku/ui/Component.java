package com.osiris.desku.ui;

import com.osiris.desku.App;
import com.osiris.desku.UI;
import com.osiris.desku.ui.display.Text;
import com.osiris.desku.ui.event.ClickEvent;
import com.osiris.desku.ui.event.ScrollEvent;
import com.osiris.desku.ui.layout.Horizontal;
import com.osiris.desku.ui.layout.Overlay;
import com.osiris.desku.ui.layout.SmartLayout;
import com.osiris.desku.ui.layout.Vertical;
import com.osiris.desku.utils.GodIterator;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Component<T extends Component<?>> {
    private static final AtomicInteger idCounter = new AtomicInteger();

    static {
        try {
            String styles = "#outlet * {\n" +
                    "  display: flex;\n" +  // All children of outlet will be flex
                    "}\n";
            App.appendToGlobalCSS(styles);
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
    /**
     * Executed when a child was added on the Java side. <br>
     *
     * @see AddedChildEvent
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
     * Executed when a attribute change was made on the Java side. <br>
     * Note that style changes are handled by {@link #onStyleChanged}. <br>
     */
    public final Event<AttributeChangeEvent> onAttributeChanged = new Event<>();
    /**
     * Do not add actions via this variable, use {@link #onClick(Consumer)} instead.
     */
    public final Event<ClickEvent> _onClick = new Event<>();
    /**
     * Do not add actions via this variable, use {@link #onScroll(Consumer)} instead.
     */
    public final Event<ScrollEvent> _onScroll = new Event<>();
    /**
     * Gets executed when this component <br>
     * was attached to the UI.
     */
    public final Event<Void> _onAttached = new Event<>();
    public final ConcurrentHashMap<String, String> style = new ConcurrentHashMap<>();
    /**
     * Gets set to false in {@link AddedChildEvent}. <br>
     */
    public boolean isFirstAdd = true;
    /**
     * True if this component is attached to a UI. <br>
     * Gets set to false if this component was removed. <br>
     */
    private boolean isAttached = false;
    public T setAttached(boolean b){
        isAttached = b;
        _onAttached.execute(null);
        return _this;
    }
    public boolean isAttached(){
        return isAttached;
    }
    /**
     * The instance of the extending class. <br>
     * Is returned in pretty much all methods, to allow method chaining by returning
     * the extending class instead of {@link Component}.
     */
    public T _this = (T) this;
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
    public MyElement element;
    public Consumer<Component<?>> _remove = child -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (children.contains(child)) {
            children.remove(child);
            if (child.element.parent() != null)
                child.element.remove();
            child.update();
            if (!ui.isLoading.get())
                ui.executeJavaScript(ui.jsGetComp("comp", id) +
                                ui.jsGetComp("childComp", child.id) +
                                "comp.removeChild(childComp);\n",
                        "internal", 0);
            child.isAttached = false;
            onRemovedChild.execute(child);
        }
    };
    public Consumer<AddedChildEvent> _add = (e) -> {

        // Perform in-memory update
        if (e.otherChildComp == null) { // add
            children.add(e.childComp);
            e.childComp.update();
            element.appendChild(e.childComp.element);
        } else if (e.isInsert) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            e.childComp.update();
            element.insertChildren(iOtherComp, e.childComp.element);
        } else if (e.isReplace) {
            int iOtherComp = children.indexOf(e.otherChildComp);
            children.set(iOtherComp, e.childComp);
            e.childComp.update();
            element.insertChildren(iOtherComp, e.childComp.element);
        }

        // Perform actual UI update
        UI ui = UI.get();
        if (!ui.isLoading.get()){
            if(!this.isAttached) {
                // Means that this (parent) was not attached yet,
                // thus we postpone the addition of child to the end of UI.access()
                ui.attachWhenAccessEnds(this, e.childComp, e);
            } else{
                ui.attachToParent(this, e.childComp, e);
            }
        }

        // Execute listeners
        onAddedChild.execute(e);
        if (e.isReplace) _remove.accept(e.otherChildComp);// Removes from children, (node) children, and actual UI
    };
    public Consumer<Attribute> _styleChange =  attribute -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (attribute.getValue().isEmpty()) {

            // Remove style
            style.remove(attribute.getKey());
            String style = element.hasAttr("style") ? element.attributes().get("style") : "";
            int iKeyFirstChar = style.indexOf(attribute.getKey());
            if (iKeyFirstChar == -1) return; // Already doesn't exist, so no removal is needed
            style = style.substring(0, iKeyFirstChar) + style.substring(style.indexOf(";", iKeyFirstChar) + 1);
            element.attr("style", style); // Change in-memory representation

            // Update UI
            if (!ui.isLoading.get()){
                executeJS("comp.style." + Theme.getJSCompatibleCSSKey(attribute.getKey())
                        + " = ``;\n"); // Change UI representation
            }
        } else {

            // Add or change style
            style.put(attribute.getKey(), attribute.getValue());

            String style = element.hasAttr("style") ? element.attributes().get("style") : "";
            style += attribute.getKey() + ": " + attribute.getValue() + ";";
            element.attr("style", style); // Change in-memory representation

            // Update UI
            if (!ui.isLoading.get()){
                executeJS("comp.style." + Theme.getJSCompatibleCSSKey(attribute.getKey())
                        + " = `" + attribute.getValue() + "`;\n"); // Change UI representation
            }
        }
        onStyleChanged.execute(attribute);
    };
    public Consumer<AttributeChangeEvent> _attributeChange = e -> {
        UI ui = UI.get(); // Necessary for updating the actual UI via JavaScript
        if (e.isInsert) { // Add or change attribute
            element.attr(e.attribute.getKey(), e.attribute.getValue()); // Change in-memory representation
            if (!ui.isLoading.get()){
                executeJS("comp.setAttribute(`" + e.attribute.getKey()
                        + "`, `" + e.attribute.getValue() + "`);\n"); // Change UI representation
            }

        } else {// Remove attribute
            element.removeAttr(e.attribute.getKey()); // Change in-memory representation
            if (!ui.isLoading.get()){
                executeJS("comp.removeAttribute(`" + e.attribute.getKey() + "`);\n"); // Change UI representation
            }
        }
        onAttributeChanged.execute(e);
    };
    boolean changedAddToSupportScroll = false;

    public Component() {
        this("c");
    }

    public Component(String tag) {
        this.element = new MyElement(this, tag);
        element.attr("java-id", String.valueOf(id));
    }

    private static ArrayList<Component<?>> toList(Iterable<Component<?>> comps) {
        ArrayList<Component<?>> list = new ArrayList<>();
        for (Component<?> c : comps) {
            list.add(c);
        }
        return list;
    }

    /**
     * Executes the provided JavaScript code now, or later
     * if this component is not attached yet. <br>
     * Your code will be encapsulated in a try/catch block and errors logged to
     * the clients JavaScript console. <br>
     * A reference of this component will be added, thus you can access
     * this component via the "comp" variable in your provided JavaScript code.
     */
    public T executeJS(String code){
        UI ui = UI.get();
        if(isAttached){
            ui.executeJavaScript(ui.jsGetComp("comp", id) +
                            code,
                    "internal", 0);
        } else{ // Execute code once attached
            _onAttached.addOneTimeAction((event, action) -> {
                ui.executeJavaScript(ui.jsGetComp("comp", id) +
                                code,
                        "internal", 0);
            }, AL::warn);
        }
        return _this;
    }

    /**
     * Executes the provided code synchronously in the current thread. <br>
     *
     * @param code the code to be executed now, contains this component as parameter.
     */
    public T now(Consumer<T> code) {
        code.accept(_this);
        return _this;
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
                code.accept(_this);
                UI.remove(this);
            }
        };
        UI.set(ui, t);
        t.start();
        return _this;
    }

    /**
     * Same as {@link #later(Consumer)} but
     * adds an overlay that
     * shows the text "Loading..." and dims/darkens this component,
     * until the async task finishes.
     */
    public T laterWithOverlay(BiConsumer<T, Overlay> code) {
        later(_this -> {
            Overlay overlay = new Overlay(this)
                    .childCenter().putStyle("background", "rgba(0,0,0,0.3)")
                    .sizeFull();
            add(overlay);
            overlay.add(new Text("Loading...")
                    .putStyle("color", "white").sizeXL().selfCenter());
            overlay.add(new Text("This might take a while, please be patient.")
                    .putStyle("color", "white").sizeS().selfCenter());
            code.accept(this._this, overlay);
            remove(overlay);
        });
        return _this;
    }

    public T add(Iterable<Component<?>> comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return _this;
    }

    public T add(Component<?>... comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, c -> {
            _add.accept(new AddedChildEvent(c, null, false, false));
        });
        return _this;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public T addAt(int index, Component<?> comp) {
        if (comp == null) return _this;
        Component<?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, true, false));
        return _this;
    }

    /**
     * @throws IndexOutOfBoundsException
     */
    public T replaceAt(int index, Component<?> comp) {
        if (comp == null) return _this;
        Component<?> otherChildComp = children.get(index);
        _add.accept(new AddedChildEvent(comp, otherChildComp, false, true));
        return _this;
    }

    /**
     * Does nothing if newComp or oldComp is null.
     *
     * @throws IndexOutOfBoundsException if oldComp does not exist in {@link #children}.
     */
    public T replace(Component<?> oldComp, Component<?> newComp) {
        if (oldComp == null || newComp == null) return _this;
        if (!children.contains(oldComp))
            throw new IndexOutOfBoundsException("Provided old component to be replaced does not exist in children!");
        _add.accept(new AddedChildEvent(newComp, oldComp, false, true));
        return _this;
    }

    public T removeAll() {
        for (Component<?> child : children) {
            _remove.accept(child);
        }
        return _this;
    }

    public T remove(Component<?>... comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, _remove);
        return _this;
    }

    public T remove(Iterable<Component<?>> comps) {
        if (comps == null) return _this;
        GodIterator.forEach(comps, _remove);
        return _this;
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
        return _this;
    }

    public T putStyle(String key, String val) {
        _styleChange.accept(new Attribute(key, val));
        return _this;
    }

    public T removeStyle(String key) {
        _styleChange.accept(new Attribute(key, ""));
        return _this;
    }

    /**
     * Adds the attribute/key without its value.
     */
    public T putAttribute(String key) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, ""), true));
        return _this;
    }

    public T putAttribute(String key, String val) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, val), true));
        return _this;
    }

    public T removeAttribute(String key) {
        _attributeChange.accept(new AttributeChangeEvent(new Attribute(key, ""), false));
        return _this;
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
        return _this;
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

        return _this;
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
        return _this;
    }

    /**
     * @see Element#html(String)
     */
    public T innerHTML(Component<?> comp) {
        remove(children);
        add(comp);
        return _this;
    }

    public T sizeFull() {
        size("100%", "100%");
        return _this;
    }

    /**
     * Sets width and height of the target component and return it for method chaining.
     */
    public T size(String width, String height) {
        putStyle("width", width);
        putStyle("height", height);
        return _this;
    }

    /**
     * Sets width of the target component and return it for method chaining.
     */
    public T width(String s) {
        putStyle("width", s);
        return _this;
    }

    /**
     * Sets height of the target component and return it for method chaining.
     */
    public T height(String s) {
        putStyle("height", s);
        return _this;
    }

    public T padding(boolean b) {
        if (b) putStyle("padding", "var(--space-s)");
        else removeStyle("padding");
        return _this;
    }

    public T padding(String s) {
        putStyle("padding", s);
        return _this;
    }

    public T paddingLeft(String s) {
        putStyle("padding-left", s);
        return _this;
    }

    public T paddingRight(String s) {
        putStyle("padding-right", s);
        return _this;
    }

    public T paddingTop(String s) {
        putStyle("padding-top", s);
        return _this;
    }

    public T paddingBottom(String s) {
        putStyle("padding-bottom", s);
        return _this;
    }

    public T margin(boolean b) {
        if (b) putStyle("margin", "var(--space-s)");
        else removeStyle("margin");
        return _this;
    }

    public boolean isVisible() {
        return !style.containsKey("visibility");
    }

    public T visible(boolean b) {
        if (b) {
            removeStyle("display");
            removeStyle("visibility");
        } else {
            putStyle("display", "none");
            putStyle("visibility", "hidden");
        }
        return _this;
    }

    /**
     * Makes this component scrollable. <br>
     * Note that you must also set the width and height for this to work, <br>
     * and the min width and height for the child components.
     *
     * @param b if true this component will be scrollable, otherwise not.
     */
    public T scrollable(boolean b, String width, String height, String minChildWidth, String minChildHeight) {
        // If we want to continue using flex display
        // together with scroll, items will be shrunk to 0pixel height
        // and thus making them invisible to the user and the scroll
        // bar not appearing. The official workaround: https://stackoverflow.com/a/21541021
        // is not optimal in our case, since the child containers style will not be inherited.
        // Thus, we do the following:
        if (width != null && !width.isEmpty()) width(width);
        if (height != null && !height.isEmpty()) height(height);
        if (b) {
            if (!changedAddToSupportScroll) {
                changedAddToSupportScroll = true;
                for (Component<?> c : children) {
                    c.putStyle("min-width", minChildWidth);
                    c.putStyle("min-height", minChildHeight);
                }
                Consumer<AddedChildEvent> superAdd = _add;
                _add = e -> {
                    e.childComp.putStyle("min-width", minChildWidth);
                    e.childComp.putStyle("min-height", minChildHeight);
                    superAdd.accept(e);
                };
            }
            overflowAuto();
        } else {
            removeStyle("overflow");
        }
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T overflowDefault() {
        removeStyle("overflow");
        return _this;
    }

    /**
     * By default, the overflow is visible, meaning that it is not clipped and it renders outside the element's box.
     */
    public T overflowVisible() {
        putStyle("overflow", "visible");
        return _this;
    }

    /**
     * With the hidden value, the overflow is clipped, and the rest of the content is hidden.
     */
    public T overflowHidden() {
        putStyle("overflow", "hidden");
        return _this;
    }

    /**
     * Setting the value to scroll, the overflow is clipped and a scrollbar is added to scroll inside the box.
     * Note that this will add a scrollbar both horizontally and vertically (even if you do not need it).
     * Thus {@link #overflowAuto()} might be better suited.
     */
    public T overflowScroll() {
        putStyle("overflow", "scroll");
        return _this;
    }

    /**
     * The auto value is similar to scroll, but it adds scrollbars only when necessary.
     */
    public T overflowAuto() {
        putStyle("overflow", "auto");
        return _this;
    }

    /**
     * By default, flex items are laid out in the source order. <br>
     * However, the order property controls the order in which they appear in the flex container. <br>
     * Items with the same order revert to source order. <br>
     * Default: 0 <br>
     */
    public T order(int i) {
        putStyle("order", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T orderDefault() {
        removeStyle("order");
        return _this;
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
        putStyle("flex-grow", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T growDefault() {
        removeStyle("flex-grow");
        return _this;
    }

    /**
     * This defines the ability for a flex item to shrink if necessary. <br>
     * Negative numbers are invalid. <br>
     * Default: 1 <br>
     */
    public T shrink(int i) {
        putStyle("flex-shrink", String.valueOf(i));
        return _this;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T shrinkDefault() {
        removeStyle("flex-shrink");
        return _this;
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
        return _this;
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
        return _this;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart()
     */
    public T selfEnd() {
        putStyle("align-self", "flex-end");
        return _this;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart()
     */
    public T selfCenter() {
        putStyle("align-self", "center");
        return _this;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart()
     */
    public T selfAuto() {
        putStyle("align-self", "auto");
        return _this;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart()
     */
    public T selfStretch() {
        putStyle("align-self", "stretch");
        return _this;
    }

    /**
     * Aligns items top to bottom.
     */
    public T childVertical() {
        putStyle("flex-direction", "column");
        return _this;
    }

    /**
     * (Default) Aligns items left to right in ltr; right to left in rtl.
     */
    public T childHorizontal() {
        putStyle("flex-direction", "row");
        return _this;
    }

    /**
     * Vertical child layout. <br>
     * Creates, adds and returns a new child layout with vertical component alignment.
     */
    public Vertical verticalCL() {
        Vertical layout = new Vertical();
        add(layout);
        return layout;
    }

    /**
     * Horizontal child layout. <br>
     * Creates, adds and returns a new child layout with horizontal component alignment.
     */
    public Horizontal horizontalCL() {
        Horizontal layout = new Horizontal();
        add(layout);
        return layout;
    }

    /**
     * Smart child layout. <br>
     * Creates, adds and returns a new smart child layout.
     */
    public SmartLayout smartCL() {
        SmartLayout layout = new SmartLayout();
        add(layout);
        return layout;
    }

    /**
     * justify-content (along primary axis) <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public T childStart() {
        putStyle("justify-content", "flex-start");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public T childEnd() {
        putStyle("justify-content", "flex-end");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * center: items are centered along the line
     */
    public T childCenter() {
        putStyle("justify-content", "center");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-between: items are evenly distributed in the line; first item is on the start line, last item on the end line
     */
    public T childSpaceBetween() {
        putStyle("justify-content", "space-between");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-around: items are evenly distributed in the line with equal space around them.
     * Note that visually the spaces aren't equal, since all the items
     * have equal space on both sides. The first item will have one unit of space
     * against the container edge, but two units of space between
     * the next item because that next item has its own spacing that applies.
     */
    public T childSpaceAround() {
        putStyle("justify-content", "space-around");
        return _this;
    }

    /**
     * justify-content (along primary axis) <br>
     * space-evenly: items are distributed so that the spacing
     * between any two items (and the space to the edges) is equal.
     */
    public T childSpaceEvenly() {
        putStyle("justify-content", "space-around");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public T childStart2() {
        putStyle("align-items", "flex-start");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public T childEnd2() {
        putStyle("align-items", "flex-end");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * center: items are centered along the line
     */
    public T childCenter2() {
        putStyle("align-items", "center");
        return _this;
    }

    /**
     * align-items (along secondary axis) <br>
     * stretch: stretch to fill the container (still respect min-width/max-width)
     */
    public T childStretch2() {
        putStyle("align-items", "stretch");
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public T childGap(boolean b) {
        if (b) putStyle("gap", "var(--space-s)");
        else removeStyle("gap");
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public T childGap(String s) {
        putStyle("gap", s);
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between rows/Y-Axis/height.
     */
    public T childGapY(String s) {
        putStyle("row-gap", s);
        return _this;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between columns/X-Axis/width.
     */
    public T childGapX(String s) {
        putStyle("column-gap", s);
        return _this;
    }

    /**
     * Adds a CSS class to this component.
     */
    public T addClass(String s) {
        String classes = element.attr("class");
        classes += " " + s;
        putAttribute("class", classes);
        return _this;
    }

    /**
     * Removes a CSS class from this component.
     */
    public T removeClass(String s) {
        String classes = element.attr("class");
        classes = classes.replace(s, "");
        putAttribute("class", classes);
        return _this;
    }

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
        return this._this;
    }

    /**
     * Adds a listener that gets executed when this component <br>
     * was clicked by the user (a JavaScript click event was thrown). <br>
     *
     * @see UI#registerJSListener(String, Component, Consumer)
     */
    public T onScroll(Consumer<ScrollEvent<T>> code) {
        _onScroll.addAction((event) -> code.accept(event));
        Component<T> _this = this;
        UI.get().registerJSListener("scroll", _this,
                "message = `{\"isReachedEnd\": \"` + (Math.abs(event.target.scrollHeight - event.target.scrollTop - event.target.clientHeight) < 1) + `\"," +
                        " \"scrollHeight\": \"` + event.target.scrollHeight + `\"," +
                        " \"scrollTop\": \"` + event.target.scrollTop + `\"," +
                        " \"clientHeight\": \"` + event.target.clientHeight + `\"," +
                        " \"eventAsJson\":` + message + `}`;\n",
                (msg) -> {
                    _onScroll.execute(new ScrollEvent<T>(msg, (T) _this)); // Executes all listeners
                });
        return this._this;
    }

    public Component<?> firstChild() {
        return children.get(0);
    }

    public Component<?> lastChild() {
        return children.get(children.size() - 1);
    }

    //
    // Listeners
    //
    public static class AttributeChangeEvent {
        public final Attribute attribute;
        public final boolean isInsert;

        public AttributeChangeEvent(Attribute attribute, boolean isInsert) {
            this.attribute = attribute;
            this.isInsert = isInsert;
        }
    }

    public static class AddedChildEvent {
        /**
         * Component that got added.
         */
        public Component<?> childComp;
        /**
         * Should only be relevant and not null when either
         * {@link #isInsert} or {@link #isReplace} is true.
         */
        public Component<?> otherChildComp;
        /**
         * If true, then {@link #otherChildComp} should now be the next child in the list after {@link #childComp}.
         */
        public boolean isInsert;
        /**
         * If true, then {@link #otherChildComp} should NOT exist in the list anymore.
         */
        public boolean isReplace;
        /**
         * True if this is the first time {@link #childComp} was added to another component.
         */
        public boolean isFirstAdd;

        public AddedChildEvent(Component<?> childComp, Component<?> otherChildComp, boolean isInsert, boolean isReplace) {
            this.childComp = childComp;
            this.isFirstAdd = childComp.isFirstAdd;
            childComp.isFirstAdd = false;
            this.otherChildComp = otherChildComp;
            this.isInsert = isInsert;
            this.isReplace = isReplace;
        }
    }
}
