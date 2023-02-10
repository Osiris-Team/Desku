package com.osiris.desku.ui;

import com.osiris.desku.UI;
import com.osiris.desku.ui.events.ClickEvent;
import com.osiris.events.Event;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Tag;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class Component<T> {
    private static final AtomicInteger idCounter = new AtomicInteger();
    /**
     * Equals the attribute "java-id" inside HTML and thus useful for finding this object via JavaScript. <br>
     * Example: The code below will return the object with the java-id = 5.
     * <pre>
     *     var element = document.querySelectorAll('[java-id="5"]')[0];
     * </pre>
     */
    public final int id = idCounter.getAndIncrement();
    protected final ConcurrentHashMap<String, String> style = new ConcurrentHashMap<>();
    public final CopyOnWriteArrayList<Component<?>> children = new CopyOnWriteArrayList<>();
    /**
     * List of {@link UI}s this component is attached to.
     */
    public final CopyOnWriteArrayList<UI> uis = new CopyOnWriteArrayList<>();
    /**
     * Executed when a child was added on the Java side.
     */
    public final Event<Component<?>> onAddedChild = new Event<>();
    /**
     * Executed when a child was removed on the Java side.
     */
    public final Event<Component<?>> onRemovedChild = new Event<>();
    /**
     * Executed when a style change was made on the Java side.
     */
    public final Event<Attribute> onStyleChanged = new Event<>();
    /**
     * Executed when a JavaScript listener was added via Java.
     */
    public final Event<EventType> onJSListenerAdded = new Event<>();
    /**
     * Executed when a JavaScript listener was removed via Java.
     */
    public final Event<EventType> onJSListenerRemoved = new Event<>();
    /**
     * Executed when this component was clicked by the user (a JavaScript click event was thrown). <br>
     * Use the {@link #onClick(Consumer)} method. Do not add actions
     * directly via this variable, since it will only work if this component
     * was not yet attached once to the UI.
     */
    public final Event<ClickEvent> _onClick = new Event<>();
    /**
     * The instance of the extending class. <br>
     * Is returned in pretty much all methods, to allow method chaining by returning
     * the extending class instead of {@link Component}.
     */
    public T target;
    /**
     * Jsoup {@link Element} that can be used to convert this
     * {@link Component} into an actual HTML string.
     */
    public Element element;

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, String tag) {
        this.target = target;
        this.element = new Element(tag);
        element.attr("java-id", "" + id);
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, Tag tag, String baseUri, Attributes attributes) {
        this.target = target;
        this.element = new Element(tag, baseUri, attributes);
        element.attr("java-id", "" + id);
    }

    /**
     * <p style="color: red">Must be called before any other method in this class!</p>
     *
     * @param target the object to be styled.
     */
    public void init(T target, Tag tag, String baseUri) {
        this.target = target;
        this.element = new Element(tag, baseUri);
        element.attr("java-id", "" + id);
    }

    public T add(Collection<Component<?>> comp) {
        if (comp == null) return target;
        children.addAll(comp);
        for (Component<?> c : comp) {
            onAddedChild.execute(c);
        }
        return target;
    }

    public T add(Component<?>... comp) {
        if (comp == null) return target;
        children.addAll(Arrays.asList(comp));
        for (Component<?> c : comp) {
            onAddedChild.execute(c);
        }
        return target;
    }

    public T remove(Component<?>... comp) {
        if (comp == null) return target;
        children.removeAll(Arrays.asList(comp));
        for (Component<?> c : comp) {
            onRemovedChild.execute(c);
        }
        return target;
    }

    public T remove(Collection<Component<?>> comp) {
        if (comp == null) return target;
        children.removeAll(comp);
        for (Component<?> c : comp) {
            onRemovedChild.execute(c);
        }
        return target;
    }

    public T stylePut(String key, String val) {
        style.put(key, val);
        onStyleChanged.execute(new Attribute(key, val));
        return target;
    }

    public T styleRemove(String key) {
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

    public void forEachChildRecursive(Consumer<Component<?>> code) {
        for (Component<?> child : this.children) {
            forEachChildRecursive(child, code);
        }
    }

    public void forEachChildRecursive(Component<?> comp, Consumer<Component<?>> code) {
        code.accept(comp);
        for (Component<?> child : comp.children) {
            forEachChildRecursive(child, code);
        }
    }

    public T sizeFull() {
        size("100%", "100%");
        return target;
    }

    /**
     * Sets width and height of the target component and return it for method chaining.
     */
    public T size(String width, String height) {
        stylePut("width", width);
        stylePut("height", height);
        return target;
    }

    /**
     * Sets width of the target component and return it for method chaining.
     */
    public T width(String s) {
        stylePut("width", s);
        return target;
    }

    /**
     * Sets height of the target component and return it for method chaining.
     */
    public T height(String s) {
        stylePut("height", s);
        return target;
    }


    public T padding(boolean b) {
        if (b) stylePut("padding", "var(--space-m)");
        else styleRemove("padding");
        return target;
    }

    public T padding(String s) {
        stylePut("padding", s);
        return target;
    }
    public T paddingLeft(String s) {
        stylePut("padding-left", s);
        return target;
    }
    public T paddingRight(String s) {
        stylePut("padding-right", s);
        return target;
    }
    public T paddingTop(String s) {
        stylePut("padding-top", s);
        return target;
    }
    public T paddingBottom(String s) {
        stylePut("padding-bottom", s);
        return target;
    }

    public T margin(boolean b) {
        if (b) stylePut("margin", "var(--space-m)");
        else styleRemove("margin");
        return target;
    }

    public T spacing(boolean b) {
        if (b) stylePut("spacing", "var(--space-m)");
        else styleRemove("spacing");
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T overflowDefault() {
        styleRemove("overflow");
        return target;
    }

    /**
     * By default, the overflow is visible, meaning that it is not clipped and it renders outside the element's box.
     */
    public T overflowVisible() {
        stylePut("overflow", "visible");
        return target;
    }

    /**
     * With the hidden value, the overflow is clipped, and the rest of the content is hidden.
     */
    public T overflowHidden() {
        stylePut("overflow", "hidden");
        return target;
    }

    /**
     * Setting the value to scroll, the overflow is clipped and a scrollbar is added to scroll inside the box.
     * Note that this will add a scrollbar both horizontally and vertically (even if you do not need it).
     * Thus {@link #overflowAuto()} might be better suited.
     */
    public T overflowScroll() {
        stylePut("overflow", "scroll");
        return target;
    }

    /**
     * The auto value is similar to scroll, but it adds scrollbars only when necessary.
     */
    public T overflowAuto() {
        stylePut("overflow", "auto");
        return target;
    }

    /**
     * By default, flex items are laid out in the source order. <br>
     * However, the order property controls the order in which they appear in the flex container. <br>
     * Items with the same order revert to source order. <br>
     * Default: 0 <br>
     */
    public T order(int i) {
        stylePut("order", "" + i);
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T orderDefault() {
        styleRemove("order");
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
        stylePut("grow", "" + i);
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T growDefault() {
        styleRemove("grow");
        return target;
    }


    /**
     * This defines the ability for a flex item to shrink if necessary. <br>
     * Negative numbers are invalid. <br>
     * Default: 1 <br>
     */
    public T shrink(int i) {
        stylePut("shrink", "" + i);
        return target;
    }

    /**
     * Removes this style attribute from {@link #style},
     * thus enforcing its default state/style.
     */
    public T shrinkDefault() {
        styleRemove("shrink");
        return target;
    }

    /**
     * By default, flex items will all try to fit onto one line.
     * You can change that and allow the items to wrap as needed with this property. <br>
     * false/nowrap (default): all flex items will be on one line <br>
     * true/wrap: flex items will wrap onto multiple lines, from top to bottom. <br>
     */
    public T wrap(boolean b) {
        if (b) stylePut("flex-wrap", "wrap");
        else stylePut("flex-wrap", "nowrap");
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
        stylePut("align-self", "flex-start");
        return target;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart()
     */
    public T selfEnd() {
        stylePut("align-self", "flex-end");
        return target;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart()
     */
    public T selfCenter() {
        stylePut("align-self", "center");
        return target;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart()
     */
    public T selfAuto() {
        stylePut("align-self", "auto");
        return target;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart()
     */
    public T selfStretch() {
        stylePut("align-self", "stretch");
        return target;
    }

    /**
     * Aligns items top to bottom.
     */
    public T childVertical() {
        stylePut("flex-direction", "column");
        return target;
    }

    /**
     * (Default) Aligns items left to right in ltr; right to left in rtl.
     */
    public T childHorizontal() {
        stylePut("flex-direction", "row");
        return target;
    }

    /**
     * justify-content <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public T childStart() {
        stylePut("justify-content", "flex-start");
        return target;
    }

    /**
     * justify-content <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public T childEnd() {
        stylePut("justify-content", "flex-end");
        return target;
    }

    /**
     * justify-content <br>
     * center: items are centered along the line
     */
    public T childCenter() {
        stylePut("justify-content", "center");
        return target;
    }

    /**
     * justify-content <br>
     * space-between: items are evenly distributed in the line; first item is on the start line, last item on the end line
     */
    public T childSpaceBetween() {
        stylePut("justify-content", "space-between");
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
        stylePut("justify-content", "space-around");
        return target;
    }

    /**
     * justify-content <br>
     * space-evenly: items are distributed so that the spacing
     * between any two items (and the space to the edges) is equal.
     */
    public T childSpaceEvenly() {
        stylePut("justify-content", "space-around");
        return target;
    }


    /**
     * align-items <br>
     * stretch: stretch to fill the container (still respect min-width/max-width)
     */
    public T childStretch() {
        stylePut("align-items", "stretch");
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public T childGap(String s) {
        stylePut("gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between rows/Y-Axis/height.
     */
    public T childGapY(String s) {
        stylePut("row-gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between columns/X-Axis/width.
     */
    public T childGapX(String s) {
        stylePut("column-gap", s);
        return target;
    }

    //
    // Listeners
    //

    /**
     * @see #_onClick
     */
    public T onClick(Consumer<ClickEvent> code) {
        _onClick.addAction((event) -> code.accept(event));
        onJSListenerAdded.execute(EventType.CLICK);
        // TODO onJSListenerRemoved
        return target;
    }
}
