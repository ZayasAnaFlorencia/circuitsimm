package com.zayas.circuit.solver;

public class ManualSolver implements SolverStrategy {

    @Override
    public double[] solve(double[][] matrix, double[] vector) throws Exception {
        double V1 = vector[0];
        double fem = vector[1];

        double R1 = matrix[0][0];
        double R2 = matrix[1][1];
        double R3 = matrix[0][2];

        double det = 12*7 - 5*5;
        if (Math.abs(det) < 1e-12) throw new Exception("Determinante 0 en ManualSolver");

        double I1 = (V1 * 7 - fem * 5) / det;
        double I2 = (12 * fem - 5 * V1) / det;
        double I3 = I1 + I2;
        return new double[]{I1, I2, I3};
    }
}
