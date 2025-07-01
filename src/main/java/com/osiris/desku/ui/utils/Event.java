package com.osiris.desku.ui.utils;

import com.osiris.events.Action;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class Event<T> extends com.osiris.events.Event<T> {
    public final List<T> allExecutionObjects = new CopyOnWriteArrayList<>();

    /**
     * If true, this event can also store all its executions ever performed via {@link #execute(Object)}
     * and re-executes them every time after a new action is added, for that action only. <br>
     * <br>
     * Meaning an action that is added later will still receive past executions and doesn't miss anything.
     * This is useful if you don't want an action to miss an execution of this event.<br>
     * <br>
     * This IS thread-safe.
     * @param <T>
     */
    public boolean storeExecutions = false;

    public Event() {
        this(false);
    }

    public Event(boolean storeExecutions) {
        super();
        this.storeExecutions = storeExecutions;
    }

    public Event(boolean storeExecutions, List<Action<T>> actions) {
        super(actions);
        this.storeExecutions = storeExecutions;
    }

    @Override
    public com.osiris.events.Event<T> execute(T t) {
        super.execute(t);
        if(storeExecutions)
            allExecutionObjects.add(t);
        return this;
    }

    @Override
    public Action<T> addAction(Action<T> action) {
        super.addAction(action);

        if(storeExecutions){
            for (T obj : allExecutionObjects) {
                //for (Action<T> action : actions) {
                try {
                    if (!markActionAsRemovableIfNeeded(action)) {
                        action.onEvent.accept(action, obj);
                        action.executionCount++;
                        //if(action.isSkipNextActions) break;
                    }
                } catch (Exception e) {
                    action.onException.accept(e);
                }
                //}
                removeActionsToRemove();
            }
        }

        return action;
    }
}
