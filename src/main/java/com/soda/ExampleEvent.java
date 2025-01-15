package com.soda;

import lombok.Getter;

@Getter
public class ExampleEvent extends Event {
    private String eventID;
    private String eventName;

    public ExampleEvent(String eventID, String eventName) {
        super();
        this.eventID = eventID;
        this.eventName = eventName;
    }
}
