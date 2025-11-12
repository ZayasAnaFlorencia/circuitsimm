package com.zayas.circuit.model;

public class Battery extends Component {
    private double voltage;
    public Battery() {}
    public Battery(String name, double v){ super(name); this.voltage = v; }
    public double getVoltage(){ return voltage; }
    public void setVoltage(double v){ this.voltage = v; }
    @Override public String toString(){ return "Battery{name='"+name+"', voltage="+voltage+"}"; }
}
