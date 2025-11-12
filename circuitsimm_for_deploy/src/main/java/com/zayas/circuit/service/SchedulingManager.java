package com.zayas.circuit.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SchedulingManager {

    private final CircuitService circuitService;
    private final PerformanceMonitorService monitor;

    public SchedulingManager(CircuitService cs, PerformanceMonitorService pm){
        this.circuitService = cs;
        this.monitor = pm;
    }

    public Map<String,Object> runSchedulingTest(String schedulerType, String complexity) {
        List<Map<String,Object>> processes = createProcesses(complexity, 3);

        monitor.start();
        List<Map<String,Object>> order;
        switch (schedulerType.toLowerCase()){
            case "fcfs": order = fcfs(processes); break;
            case "round_robin": order = roundRobin(processes, 2); break;
            case "sjf": order = sjf(processes); break;
            default: order = fcfs(processes);
        }
        Map<String,Object> report = monitor.stopAndReport();
        report.put("execution_order", order);
        report.put("complexity", complexity);
        return report;
    }

    private List<Map<String,Object>> createProcesses(String complexity, int n){
        Map<String,int[]> burst = Map.of(
            "simple", new int[]{1,2,3},
            "medium", new int[]{3,4,5},
            "complex", new int[]{6,7,8}
        );
        String[] methods = {"cramer","gauss-jordan","manual"};
        List<Map<String,Object>> res = new ArrayList<>();
        for(int i=0;i<n;i++){
            Map<String,Object> p = new HashMap<>();
            p.put("pid", i+1);
            p.put("method", methods[i%methods.length]);
            p.put("arrival_time", i*0.5);
            p.put("burst_time", burst.getOrDefault(complexity, new int[]{1,2,3})[i%3]);
            res.add(p);
        }
        return res;
    }

    private List<Map<String,Object>> fcfs(List<Map<String,Object>> procs){
        procs.sort(Comparator.comparingDouble(p->(double)p.get("arrival_time")));
        List<Map<String,Object>> order = new ArrayList<>();
        double current=0;
        for (var p: procs){
            double at = (double)p.get("arrival_time");
            int bt = (int)p.get("burst_time");
            if (current < at) current = at;
            double start = current;
            long t0 = System.nanoTime();
            try{ Thread.sleep(bt*1L); } catch(Exception ignored){}
            long t1 = System.nanoTime();
            double execSecs = (t1 - t0)/1e9;
            monitor.record((String)p.get("method"), execSecs);
            current += bt;
            Map<String,Object> info = new HashMap<>(p);
            info.put("start_time", start);
            info.put("end_time", current);
            order.add(info);
        }
        return order;
    }

    private List<Map<String,Object>> roundRobin(List<Map<String,Object>> procs, int quantum){
        Queue<Map<String,Object>> q = new LinkedList<>(procs);
        double current = 0;
        Map<Integer,Integer> remaining = new HashMap<>();
        for (var p : procs) remaining.put((int)p.get("pid"), (int)p.get("burst_time"));
        List<Map<String,Object>> order = new ArrayList<>();
        while(!q.isEmpty()){
            var p = q.poll();
            int pid = (int)p.get("pid");
            int rem = remaining.get(pid);
            int use = Math.min(quantum, rem);
            long t0 = System.nanoTime();
            try{ Thread.sleep(use*1L); } catch(Exception ignored){}
            long t1 = System.nanoTime();
            monitor.record((String)p.get("method"), (t1-t0)/1e9);
            double start = current;
            current += use;
            rem -= use;
            Map<String,Object> info = new HashMap<>(p);
            info.put("start_time", start);
            info.put("end_time", current);
            info.put("quantum_used", use);
            order.add(info);
            if (rem>0){ remaining.put(pid, rem); q.add(p); }
            else { remaining.put(pid, 0); }
        }
        return order;
    }

    private List<Map<String,Object>> sjf(List<Map<String,Object>> procs){
        List<Map<String,Object>> remaining = new ArrayList<>(procs);
        double current = 0;
        List<Map<String,Object>> order = new ArrayList<>();
        while(!remaining.isEmpty()){
            List<Map<String,Object>> available = remaining.stream()
                    .filter(p -> (double)p.get("arrival_time") <= current)
                    .toList();
            if (available.isEmpty()){ current += 1; continue; }
            Map<String,Object> next = available.stream()
                    .min(Comparator.comparingInt(p -> (int)p.get("burst_time"))).get();
            int bt = (int)next.get("burst_time");
            long t0 = System.nanoTime();
            try{ Thread.sleep(bt*1L); } catch(Exception ignored){}
            long t1 = System.nanoTime();
            monitor.record((String)next.get("method"), (t1-t0)/1e9);
            double start = current;
            current += bt;
            Map<String,Object> info = new HashMap<>(next);
            info.put("start_time", start);
            info.put("end_time", current);
            order.add(info);
            remaining.remove(next);
        }
        return order;
    }
}
