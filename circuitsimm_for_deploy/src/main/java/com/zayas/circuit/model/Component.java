package com.zayas.circuit.model;

public abstract class Component {
    protected String name;
    public Component() {}
    public Component(String name) { this.name = name; }
    public String getName() { return name; }
    public void setName(String n) { name = n; }
}
