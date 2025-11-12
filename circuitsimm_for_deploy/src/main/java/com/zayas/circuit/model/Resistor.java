package com.zayas.circuit.model;

public class Resistor extends Component {
    private double resistance;
    public Resistor() {}
    public Resistor(String name, double r){ super(name); this.resistance = r; }
    public double getResistance(){ return resistance; }
    public void setResistance(double r){ this.resistance = r; }
    @Override public String toString(){ return "Resistor{name='"+name+"', resistance="+resistance+"}"; }
}
