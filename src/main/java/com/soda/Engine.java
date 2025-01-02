package com.soda;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.*;


interface Callback {
    void call(Event event);
}


@Slf4j
public class Engine {
    private final Map<Class<?>, List<Callback>> eventHandlers = new HashMap<>();
    private final Queue<Event> queue = new ConcurrentLinkedQueue<>();
    ExecutorService executorService;
    public static final Engine INSTANCE = new Engine();

    private Engine() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpuCores);
    }

    public <T extends Application> void addApplication(Class<T> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        T instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        instance.setEngine(this);
        for (Method method : declaredMethods) {
            method.setAccessible(true);
            if (method.isAnnotationPresent(EventHandler.class)) {
                log.info("Method with EventHandler annotation: {}", method.getName());
                Parameter[] parameters = method.getParameters();
                if (parameters.length != 1) {
                    throw new RuntimeException("Method with EventHandler annotation must have exactly one parameter");
                }
                if (!Event.class.isAssignableFrom(parameters[0].getType())) {
                    throw new RuntimeException("Method with EventHandler annotation must have exactly one parameter of type Event");
                }
                final T finalInstance = instance;
                eventHandlers.computeIfAbsent(parameters[0].getType(), k -> new ArrayList<>()).add((Event event) -> {
                    try {
                        method.invoke(finalInstance, event);
                    } catch (Exception e) {
                        log.error("Error invoking method", e);
                    }
                });
            }
        }

    }

    public void pubEvent(Event event) {
        queue.add(event);
    }

    public void run() {
        pubEvent(new ExampleEvent("1", "Example Event"));
        pubEvent(new ExampleEvent1("2", "Example Event 1"));

        while (true) {
            Event event = queue.poll();
            if (event == null) {
                continue;
            }
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
        }

    }

    public static void main(String[] args) {
        Engine.INSTANCE.addApplication(ExampleApplication.class);
        Engine.INSTANCE.run();
    }
}
