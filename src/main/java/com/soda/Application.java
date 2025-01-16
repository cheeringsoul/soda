package com.soda;

public class Application {
    protected Engine engine;

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    @SuppressWarnings("unused")
    public void pubEvent(Event event) {
        engine.pubEvent(event);
    }
}

