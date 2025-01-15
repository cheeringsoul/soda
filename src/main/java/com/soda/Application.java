package com.soda;

import lombok.Setter;

@Setter
public class Application {
    protected Engine engine;

    @SuppressWarnings("unused")
    public void pubEvent(Event event) {
        engine.pubEvent(event);
    }
}

