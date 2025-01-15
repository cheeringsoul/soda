package com.soda;

import lombok.Getter;

@Getter
public class ExampleEvent1 extends Event {
    private String eventID;
    private String eventName;

    public ExampleEvent1(String eventID, String eventName) {
        super();
        this.eventID = eventID;
        this.eventName = eventName;
    }
}
