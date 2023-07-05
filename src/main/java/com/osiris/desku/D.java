package com.osiris.desku;

import com.osiris.desku.ui.UI;
import com.osiris.events.Action;
import com.osiris.events.Event;
import com.osiris.jlib.logger.AL;

import java.util.List;

/**
 * Observable object/data, which simply means
 * that changing it via {@link #set(Object)} will
 * trigger a value change event, and thus notify its listeners / execute their actions. <br>
 * If an action is related to an UI which is closed, it gets removed and not executed.
 */
public class D<T> extends Event<T> {
    private T value;

    public D(T value) {
        this.value = value;
        initCleaner(30, obj -> obj != null && obj instanceof UI && !((UI) obj).isOpen(), AL::warn);
    }

    public D(T value, List<Action<T>> actions) {
        super(actions);
        this.value = value;
    }

    public T get() {
        return value;
    }

    public D<T> set(T value) {
        this.value = value;
        execute(value);
        return this;
    }

    @Override
    public Action<T> addAction(Action<T> action) {
        action.object = UI.get();
        return super.addAction(action);
    }
}
