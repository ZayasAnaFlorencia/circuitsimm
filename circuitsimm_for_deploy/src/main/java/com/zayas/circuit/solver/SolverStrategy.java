package com.zayas.circuit.solver;

public interface SolverStrategy {
    double[] solve(double[][] matrix, double[] vector) throws Exception;
}
