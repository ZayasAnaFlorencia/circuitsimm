package com.zayas.circuit.controller;

import com.zayas.circuit.model.Battery;
import com.zayas.circuit.model.CurrentSource;
import com.zayas.circuit.model.Resistor;
import com.zayas.circuit.service.CircuitService;
import com.zayas.circuit.service.ImprovedCircuitAnalyzer;
import com.zayas.circuit.service.SchedulingManager;
import com.zayas.circuit.solver.CramerSolver;
import com.zayas.circuit.solver.GaussJordanSolver;
import com.zayas.circuit.solver.ManualSolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final CircuitService circuitService;
    private final ImprovedCircuitAnalyzer analyzer;
    private final SchedulingManager schedulingManager;

    @Value("${app.reports.dir:reports}")
    private String reportsDir;

    public ApiController(CircuitService cs, ImprovedCircuitAnalyzer ia, SchedulingManager sm){
        this.circuitService = cs;
        this.analyzer = ia;
        this.schedulingManager = sm;
        try { Files.createDirectories(Paths.get(reportsDir)); } catch(Exception ignored) {}
    }

    @PostMapping("/circuit/example")
    public ResponseEntity<?> createExample(){
        circuitService.clear();
        circuitService.addComponent(new Battery("B1", 15.0));
        circuitService.addComponent(new Resistor("R1", 7.0));
        circuitService.addComponent(new Resistor("R2", 2.0));
        circuitService.addComponent(new Resistor("R3", 5.0));
        circuitService.addComponent(new CurrentSource("A", 2.0));
        circuitService.addComponent(new Battery("E", 12.58));
        return ResponseEntity.ok(Map.of("message","Circuito ejemplo creado"));
    }

    @PostMapping("/component")
    public ResponseEntity<?> addComponent(@RequestBody Map<String,Object> body){
        String type = (String) body.get("type");
        String name = (String) body.get("name");
        double value = ((Number)body.get("value")).doubleValue();
        switch(type.toLowerCase()){
            case "resistor": circuitService.addComponent(new Resistor(name,value)); break;
            case "battery": circuitService.addComponent(new Battery(name,value)); break;
            case "currentsource": circuitService.addComponent(new CurrentSource(name,value)); break;
            default: return ResponseEntity.badRequest().body(Map.of("error","Tipo desconocido"));
        }
        return ResponseEntity.ok(Map.of("message","Componente agregado"));
    }

    @PostMapping("/solver")
    public ResponseEntity<?> setSolver(@RequestParam String method){
        switch(method.toLowerCase()){
            case "cramer": analyzer.setSolver(new CramerSolver()); break;
            case "gauss-jordan": analyzer.setSolver(new GaussJordanSolver()); break;
            case "manual": analyzer.setSolver(new ManualSolver()); break;
            default: return ResponseEntity.badRequest().body(Map.of("error","Metodo desconocido"));
        }
        return ResponseEntity.ok(Map.of("message","Solver cambiado a " + method));
    }

    @PostMapping("/solve")
    public ResponseEntity<?> solve(){
        Map<String,Object> res = analyzer.solve(circuitService);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/circuit/state")
    public ResponseEntity<?> state(){
        Map<String,Object> data = new HashMap<>();
        data.put("components", circuitService.getAllComponents());
        data.put("solutions", circuitService.getSolutions());
        return ResponseEntity.ok(data);
    }

    @PostMapping("/schedule")
    public ResponseEntity<?> schedule(@RequestParam String scheduler, @RequestParam(defaultValue = "medium") String complexity){
        Map<String,Object> report = schedulingManager.runSchedulingTest(scheduler, complexity);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/report/save")
    public ResponseEntity<?> saveReport(@RequestParam(defaultValue = "report") String name) {
        try {
            Map<String,Object> solutions = circuitService.getSolutions();
            if (solutions == null || solutions.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "No hay soluciones para guardar"));
            }
            Files.createDirectories(Paths.get(reportsDir));
            String timestamp = String.valueOf(System.currentTimeMillis());
            String base = name + "_" + timestamp;
            Path txt = Paths.get(reportsDir, base + ".txt");
            Path csv = Paths.get(reportsDir, base + ".csv");

            try (BufferedWriter bw = Files.newBufferedWriter(txt)) {
                bw.write("REPORTE DE SOLUCIONES\n\n");
                for (var e : solutions.entrySet()) {
                    bw.write(e.getKey() + ": " + e.getValue() + "\n");
                }
            }

            try (BufferedWriter bw = Files.newBufferedWriter(csv)) {
                bw.write("key,value\n");
                for (var e : solutions.entrySet()) {
                    String k = e.getKey();
                    Object v = e.getValue();
                    bw.write(k + ",\"" + v.toString().replaceAll(",",";") + "\"\n");
                }
            }

            return ResponseEntity.ok(Map.of("txt", txt.toString(), "csv", csv.toString()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/report/download")
    public ResponseEntity<?> downloadReport(@RequestParam String filename) {
        try {
            Path file = Paths.get(reportsDir, filename);
            if (!Files.exists(file)) return ResponseEntity.status(404).body(Map.of("error","Archivo no encontrado"));
            InputStreamResource resource = new InputStreamResource(Files.newInputStream(file));
            String mime = Files.probeContentType(file);
            if (mime == null) mime = "application/octet-stream";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName().toString() + "\"")
                    .contentType(MediaType.parseMediaType(mime))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/lang")
    public ResponseEntity<?> setLang(@RequestParam String lang) {
        circuitService.setSolutions(Map.of("lang", lang));
        return ResponseEntity.ok(Map.of("message", "Idioma seteado a " + lang));
    }
}
