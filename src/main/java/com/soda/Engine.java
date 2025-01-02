package com.soda;

import com.soda.annotations.EventHandler;
import com.soda.annotations.AutoLoad;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j
public class Engine {
    public static final Engine INSTANCE = new Engine();
    private final ExecutorService executorService;
    private final Map<Class<?>, List<Consumer<Event>>> messageHandlerMap = new HashMap<>();

    private Engine() {
        int cpuCores = Runtime.getRuntime().availableProcessors();
        executorService = Executors.newFixedThreadPool(cpuCores);
    }

    public <T extends Application> void addApplication(Class<T> clazz) {
        Method[] declaredMethods = clazz.getDeclaredMethods();
        T instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
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
                messageHandlerMap.computeIfAbsent(parameters[0].getType(), k -> new ArrayList<>()).add((Event event) -> {
                    try {
                        method.invoke(finalInstance, event);
                    } catch (Exception e) {
                        log.error("Error invoking method", e);
                    }
                });
            }
        }

    }

    private void autoLoadApplications(String packageName) {
        Reflections reflections = new Reflections(packageName, Scanners.TypesAnnotated);
        Set<Class<?>> appClasses = reflections.getTypesAnnotatedWith(AutoLoad.class);
        for (Class<?> appClass : appClasses) {
            if (Application.class.isAssignableFrom(appClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends Application> validAppClass = (Class<? extends Application>) appClass;
                addApplication(validAppClass);
            }
        }
    }

    public void pubEvent(Event event) {
        executorService.submit(() -> messageHandlerMap.get(event.getClass()).forEach(consumer -> consumer.accept(event)));
    }

    public void run() {
        Thread.yield();
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public static void main(String[] args) {
        Engine.INSTANCE.addApplication(ExampleApplication.class);
        Engine.INSTANCE.pubEvent(new ExampleEvent("1", "Example Event"));
        Engine.INSTANCE.pubEvent(new ExampleEvent1("2", "Example Event 1"));
        Engine.INSTANCE.run();
    }
}
