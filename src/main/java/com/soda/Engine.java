package com.soda;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


interface Callback <E extends Event>{
    void call(E event);
}


public class Engine {
    private static final int QUEUE_CAPACITY = 10;
    private final Map<Class<?>, Callback> eventHandlers = new HashMap<>();
    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);

    public <T extends Application, E extends Event> void addApplication(Class<T> clazz) {
        try {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            T instance = clazz.getDeclaredConstructor().newInstance();
            instance.setEngine(this);

            for (Method method : declaredMethods) {
                method.setAccessible(true);
                if (method.isAnnotationPresent(EventHandler.class)) {
                    System.out.println("Method with EventHandler annotation: " + method.getName());
                    Parameter[] parameters = method.getParameters();
                    if (parameters.length != 1) {
                        throw new RuntimeException("Method with EventHandler annotation must have exactly one parameter");
                    }
                    if (!Event.class.isAssignableFrom(parameters[0].getType())) {
                        throw new RuntimeException("Method with EventHandler annotation must have exactly one parameter of type Event");
                    }
                    eventHandlers.put(parameters[0].getType(), (Event event) -> {
                        try {
                            method.invoke(instance, event);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pubEvent(Event event) {
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Event event = queue.take();
                Callback callback = eventHandlers.get(event.getClass());
                if (callback != null) {
                    try {
                        callback.call( event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    public static void main(String[] args) {
        Engine engine = new Engine();
        engine.addApplication(Application.class);
        engine.run();
    }
}
