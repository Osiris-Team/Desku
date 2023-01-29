package com.osiris.desku.ui.events;

public abstract class Event {
    /**
     * @param jsMessage JavaScript message containing details about the event.
     *                  Classes that extend {@link Event} should parse it in this constructor
     *                  and set relevant data to public final fields.
     */
    public Event(String jsMessage) {

    }
}
