package com.soda;

import lombok.Setter;

@Setter
public class Application {
    protected Engine engine;

    public void pubEvent(Event event) {
        engine.pubEvent(event);
    }
}

