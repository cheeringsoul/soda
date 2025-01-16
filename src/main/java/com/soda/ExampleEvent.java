package com.soda;


public class ExampleEvent extends Event {
    private final String eventID;
    private final String eventName;

    public String getEventName() {
        return eventName;
    }

    public String getEventID() {
        return eventID;
    }

    public ExampleEvent(String eventID, String eventName) {
        this.eventID = eventID;
        this.eventName = eventName;
    }
}
