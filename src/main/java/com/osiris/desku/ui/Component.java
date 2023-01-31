package com.osiris.desku.ui;

import com.osiris.desku.Route;
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
    public final ConcurrentHashMap<String, String> style = new ConcurrentHashMap<>();
    public final CopyOnWriteArrayList<Component<?>> children = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<Runnable> clickListeners = new CopyOnWriteArrayList<>();
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
        return target;
    }

    public T add(Component<?>... comp) {
        if (comp == null) return target;
        children.addAll(Arrays.asList(comp));
        return target;
    }

    public T remove(Component<?>... comp) {
        if (comp == null) return target;
        children.removeAll(Arrays.asList(comp));
        return target;
    }

    public T remove(Collection<Component<?>> comp) {
        if (comp == null) return target;
        children.removeAll(comp);
        return target;
    }

    /**
     * Must be called after changing the style or after adding/removing children to/from this component,
     * otherwise the changes won't be visible. <br>
     * If child components also changed call {@link #updateAll()} instead. <br>
     */
    public T update() {
        StringBuilder sb = new StringBuilder();
        style.forEach((key, val) -> {
            sb.append(key).append(": ").append(val).append("; ");
        });
        element.attr("style", sb.toString());
        return target;
    }

    /**
     * Performs {@link #update()} for this and all child components recursively. <br>
     * Note that you don't have to call this function, since it already gets called before showing the window,
     * in {@link Route#toDocument()}.
     */
    public T updateAll() {
        // Update this style
        update();

        // Clear children
        for (Element child : element.children()) {
            child.remove();
        }

        // Update this child list
        for (Component<?> childComp : children) {
            element.appendChild(childComp.element);
        }

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
        style.put("width", width);
        style.put("height", height);
        return target;
    }

    /**
     * Sets width of the target component and return it for method chaining.
     */
    public T width(String s) {
        style.put("width", s);
        return target;
    }

    /**
     * Sets height of the target component and return it for method chaining.
     */
    public T height(String s) {
        style.put("height", s);
        return target;
    }


    public T padding(boolean b) {
        if (b) style.put("padding", "var(--space-m)");
        else style.remove("padding");
        return target;
    }

    public T margin(boolean b) {
        if (b) style.put("margin", "var(--space-m)");
        else style.remove("margin");
        return target;
    }

    public T spacing(boolean b) {
        if (b) style.put("spacing", "var(--space-m)");
        else style.remove("spacing");
        return target;
    }

    /**
     * By default, flex items are laid out in the source order. <br>
     * However, the order property controls the order in which they appear in the flex container. <br>
     * Items with the same order revert to source order. <br>
     * Default: 0 <br>
     */
    public T order(int i) {
        style.put("order", "" + i);
        return target;
    }

    public T orderRemove() {
        style.remove("order");
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
        style.put("grow", "" + i);
        return target;
    }

    public T growRemove() {
        style.remove("grow");
        return target;
    }


    /**
     * This defines the ability for a flex item to shrink if necessary. <br>
     * Negative numbers are invalid. <br>
     * Default: 1 <br>
     */
    public T shrink(int i) {
        style.put("shrink", "" + i);
        return target;
    }

    public T shrinkRemove() {
        style.remove("shrink");
        return target;
    }

    /**
     * By default, flex items will all try to fit onto one line.
     * You can change that and allow the items to wrap as needed with this property. <br>
     * false/nowrap (default): all flex items will be on one line <br>
     * true/wrap: flex items will wrap onto multiple lines, from top to bottom. <br>
     */
    public T wrap(boolean b) {
        if (b) style.put("flex-wrap", "wrap");
        else style.put("flex-wrap", "nowrap");
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
        style.put("align-self", "flex-start");
        return target;
    }

    /**
     * The element is positioned at the end of the container.
     *
     * @see #selfStart()
     */
    public T selfEnd() {
        style.put("align-self", "flex-end");
        return target;
    }

    /**
     * The element is positioned at the center of the container.
     *
     * @see #selfStart()
     */
    public T selfCenter() {
        style.put("align-self", "center");
        return target;
    }

    /**
     * Default. The element inherits its parent container's align-items property,
     * or "stretch" if it has no parent container.
     *
     * @see #selfStart()
     */
    public T selfAuto() {
        style.put("align-self", "auto");
        return target;
    }

    /**
     * The element is positioned to fit the container.
     *
     * @see #selfStart()
     */
    public T selfStretch() {
        style.put("align-self", "stretch");
        return target;
    }

    /**
     * Aligns items top to bottom.
     */
    public T childVertical() {
        style.put("flex-direction", "column");
        return target;
    }

    /**
     * (Default) Aligns items left to right in ltr; right to left in rtl.
     */
    public T childHorizontal() {
        style.put("flex-direction", "row");
        return target;
    }

    /**
     * justify-content <br>
     * flex-start (default): items are packed toward the start of the flex-direction.
     */
    public T childStart() {
        style.put("justify-content", "flex-start");
        return target;
    }

    /**
     * justify-content <br>
     * flex-end: items are packed toward the end of the flex-direction.
     */
    public T childEnd() {
        style.put("justify-content", "flex-end");
        return target;
    }

    /**
     * justify-content <br>
     * center: items are centered along the line
     */
    public T childCenter() {
        style.put("justify-content", "center");
        return target;
    }

    /**
     * justify-content <br>
     * space-between: items are evenly distributed in the line; first item is on the start line, last item on the end line
     */
    public T childSpaceBetween() {
        style.put("justify-content", "space-between");
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
        style.put("justify-content", "space-around");
        return target;
    }

    /**
     * justify-content <br>
     * space-evenly: items are distributed so that the spacing
     * between any two items (and the space to the edges) is equal.
     */
    public T childSpaceEvenly() {
        style.put("justify-content", "space-around");
        return target;
    }


    /**
     * align-items <br>
     * stretch: stretch to fill the container (still respect min-width/max-width)
     */
    public T childStretch() {
        style.put("align-items", "stretch");
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges.
     */
    public T childGap(String s) {
        style.put("gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between rows/Y-Axis/height.
     */
    public T childGapY(String s) {
        style.put("row-gap", s);
        return target;
    }

    /**
     * The gap property explicitly controls the space between flex items.
     * It applies that spacing only between items not on the outer edges. <br>
     * Gap between columns/X-Axis/width.
     */
    public T childGapX(String s) {
        style.put("column-gap", s);
        return target;
    }

    //
    // Listeners
    //

    public T onClick(Runnable code) {
        clickListeners.add(code);
        return target;
    }

    public CopyOnWriteArrayList<Runnable> getClickListeners() {
        return clickListeners;
    }
}
