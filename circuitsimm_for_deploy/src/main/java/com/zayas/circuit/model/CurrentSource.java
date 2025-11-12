package com.zayas.circuit.model;

public class CurrentSource extends Component {
    private double current;
    public CurrentSource() {}
    public CurrentSource(String name, double i){ super(name); this.current = i; }
    public double getCurrent(){ return current; }
    public void setCurrent(double i){ this.current = i; }
    @Override public String toString(){ return "CurrentSource{name='"+name+"', current="+current+"}"; }
}
