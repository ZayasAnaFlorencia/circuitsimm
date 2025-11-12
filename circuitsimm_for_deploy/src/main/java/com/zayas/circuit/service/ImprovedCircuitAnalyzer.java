package com.zayas.circuit.service;

import com.zayas.circuit.model.Battery;
import com.zayas.circuit.model.CurrentSource;
import com.zayas.circuit.model.Resistor;
import com.zayas.circuit.solver.SolverStrategy;
import com.zayas.circuit.solver.CramerSolver;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ImprovedCircuitAnalyzer {

    private SolverStrategy solver;

    public ImprovedCircuitAnalyzer(){
        this.solver = new CramerSolver();
    }

    public void setSolver(SolverStrategy s){
        this.solver = s;
    }

    public double[][] buildMatrix(CircuitService circuitService){
        Battery b1 = (Battery) circuitService.getComponent("B1");
        Resistor r1 = (Resistor) circuitService.getComponent("R1");
        Resistor r2 = (Resistor) circuitService.getComponent("R2");
        Resistor r3 = (Resistor) circuitService.getComponent("R3");
        CurrentSource a = (CurrentSource) circuitService.getComponent("A");
        Battery e = (Battery) circuitService.getComponent("E");

        double V1 = b1 != null ? b1.getVoltage() : 15.0;
        double R1 = r1 != null ? r1.getResistance() : 7.0;
        double R2 = r2 != null ? r2.getResistance() : 2.0;
        double R3 = r3 != null ? r3.getResistance() : 5.0;
        double I_source = a != null ? a.getCurrent() : 2.0;
        double fem = e != null ? e.getVoltage() : 12.58;

        double[][] matrix = new double[][] {
                {R1, 0, R3},
                {0, R2, R3},
                {1, 1, -1}
        };
        return matrix;
    }

    public double[] buildVector(CircuitService circuitService){
        Battery b1 = (Battery) circuitService.getComponent("B1");
        Battery e = (Battery) circuitService.getComponent("E");
        CurrentSource a = (CurrentSource) circuitService.getComponent("A");

        double V1 = b1 != null ? b1.getVoltage() : 15.0;
        double fem = e != null ? e.getVoltage() : 12.58;
        double I_source = a != null ? a.getCurrent() : 2.0;

        return new double[]{V1, fem, I_source};
    }

    public Map<String, Object> solve(CircuitService circuitService) {
        try {
            double[][] matrix = buildMatrix(circuitService);
            double[] vector = buildVector(circuitService);
            double[] solutions = solver.solve(matrix, vector);

            double I1 = solutions[0], I2 = solutions[1], I3 = solutions[2];
            double V_R1 = I1 * (matrix[0][0]);
            double V_R2 = I2 * (matrix[1][1]);
            double V_R3 = I3 * (matrix[0][2]);
            double potencia = I1*I1*matrix[0][0] + I2*I2*matrix[1][1] + I3*I3*matrix[0][2];

            Map<String,Object> res = new HashMap<>();
            res.put("I1", I1);
            res.put("I2", I2);
            res.put("I3", I3);
            res.put("V_R1", V_R1);
            res.put("V_R2", V_R2);
            res.put("V_R3", V_R3);
            res.put("Potencia_total", potencia);

            boolean leyCorrientes = Math.abs(I1 + I2 - I3) < 1e-9;
            double V1 = (circuitService.getComponent("B1") instanceof Battery) ? ((Battery) circuitService.getComponent("B1")).getVoltage() : 15.0;
            double fem = (circuitService.getComponent("E") instanceof Battery) ? ((Battery) circuitService.getComponent("E")).getVoltage() : 12.58;
            boolean leyT1 = Math.abs(V1 - V_R1 - V_R3) < 1e-9;
            boolean leyT2 = Math.abs(fem - V_R2 - V_R3) < 1e-9;

            Map<String, Boolean> verification = new HashMap<>();
            verification.put("ley_corrientes", leyCorrientes);
            verification.put("ley_tensiones_malla1", leyT1);
            verification.put("ley_tensiones_malla2", leyT2);

            res.put("verification", verification);

            circuitService.setSolutions(res);
            return res;
        } catch (Exception e) {
            Map<String,Object> error = new HashMap<>();
            error.put("error", "Error al resolver: " + e.getMessage());
            return error;
        }
    }
}
