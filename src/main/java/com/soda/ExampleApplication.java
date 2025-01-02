package com.soda;

import com.soda.annotations.EventHandler;

public class ExampleApplication extends Application {

    @EventHandler
    private void handle(ExampleEvent event) {
        System.out.println("Handling event: " + event.getEventName());
    }

    @EventHandler
    public void handle(ExampleEvent1 event) {
        System.out.println("Handling event: " + event.getEventName());
    }
}
