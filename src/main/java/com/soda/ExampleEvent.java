package com.soda;

import lombok.Getter;

@Getter
public class ExampleEvent extends Event {
    private final String eventID;
    private final String eventName;

    public ExampleEvent(String eventID, String eventName) {
        super();
        this.eventID = eventID;
        this.eventName = eventName;
    }
}
