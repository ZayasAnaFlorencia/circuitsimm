package com.zayas.circuit.service;

import com.zayas.circuit.model.Component;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CircuitService {
    private final Map<String, Component> components = new ConcurrentHashMap<>();
    private final Map<String, Object> solutions = new ConcurrentHashMap<>();

    public void clear() {
        components.clear();
        solutions.clear();
    }

    public void addComponent(Component c) {
        components.put(c.getName(), c);
    }

    public void removeComponent(String name) {
        components.remove(name);
    }

    public Component getComponent(String name) {
        return components.get(name);
    }

    public Collection<Component> getAllComponents() {
        return components.values();
    }

    public void setSolutions(Map<String, Object> s) {
        solutions.clear();
        solutions.putAll(s);
    }

    public Map<String, Object> getSolutions() {
        return Map.copyOf(solutions);
    }
}
