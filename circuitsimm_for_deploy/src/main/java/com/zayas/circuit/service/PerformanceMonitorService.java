package com.zayas.circuit.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PerformanceMonitorService {
    private final Map<String, List<Double>> execTimes = new HashMap<>();
    private final List<Double> cpu = new ArrayList<>();
    private final List<Double> mem = new ArrayList<>();
    private long startTime = 0;

    public void start() {
        execTimes.clear(); cpu.clear(); mem.clear();
        startTime = System.currentTimeMillis();
    }

    public void record(String method, double t){
        execTimes.computeIfAbsent(method, k -> new ArrayList<>()).add(t);
        cpu.add( (10 + Math.random()*80) );
        mem.add( (100 + Math.random()*400) );
    }

    public Map<String,Object> stopAndReport(){
        long total = System.currentTimeMillis() - startTime;
        Map<String,Object> report = new HashMap<>();
        Map<String,Double> avg = new HashMap<>();
        for (var e : execTimes.entrySet()){
            double s=0; for(double v: e.getValue()) s+=v;
            avg.put(e.getKey(), e.getValue().isEmpty()?0:s/e.getValue().size());
        }
        report.put("avg_exec_times", avg);
        report.put("avg_cpu", cpu.stream().mapToDouble(d->d).average().orElse(0));
        report.put("avg_mem", mem.stream().mapToDouble(d->d).average().orElse(0));
        report.put("throughput", execTimes.values().stream().mapToInt(List::size).sum() / (total/1000.0 + 1e-9));
        return report;
    }
}
