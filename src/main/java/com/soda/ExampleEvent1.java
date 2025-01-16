package com.soda;

public class ExampleEvent1 extends Event {
    private final String eventID;
    private final String eventName;

    public String getEventID() {
        return eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public ExampleEvent1(String eventID, String eventName) {
        this.eventID = eventID;
        this.eventName = eventName;
    }
}
