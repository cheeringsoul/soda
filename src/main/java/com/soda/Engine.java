package com.soda;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


interface Callback {
    void call(Event event);
}


@Slf4j
public class Engine {
    private static final int QUEUE_CAPACITY = 10;
    private final Map<Class<?>, List<Callback>> eventHandlers = new HashMap<>();
    private final BlockingQueue<Event> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    ExecutorService executorService;
    public static final Engine INSTANCE = new Engine();

    private Engine() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpuCores);
    }

    public <T extends Application> void addApplication(Class<T> clazz) {
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
                    eventHandlers.computeIfAbsent(parameters[0].getType(), k -> new ArrayList<>()).add((Event event) -> {
                        try {
                            method.invoke(instance, event);
                        } catch (Exception e) {
                            log.error("Error invoking method", e);
                        }
                    });

                }
            }
        } catch (Exception e) {
            log.error("Error adding application", e);
        }
    }

    public void pubEvent(Event event) {
        try {
            queue.put(event);
        } catch (InterruptedException e) {
            log.error("Error publishing event", e);
        }
    }

    public void run() {
        pubEvent(new ExampleEvent("1", "Example Event"));
        pubEvent(new ExampleEvent1("2", "Example Event 1"));

        while (true) {
            try {
                Event event = queue.take();
                List<Callback> callbacks = eventHandlers.get(event.getClass());
                if (callbacks == null) {
                    continue;
                }
                for (Callback callback : callbacks) {
                    try {
                        callback.call(event);
                    } catch (Exception e) {
                        log.error("Error calling callback", e);
                    }
                }
            } catch (InterruptedException e) {
                log.error("Error running engine", e);
                break;
            }
        }

    }

    public static void main(String[] args) {
        Engine.INSTANCE.addApplication(ExampleApplication.class);
        Engine.INSTANCE.run();
    }
}
