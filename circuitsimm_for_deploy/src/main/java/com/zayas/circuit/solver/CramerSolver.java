package com.zayas.circuit.solver;

public class CramerSolver implements SolverStrategy {

    private double determinant(double[][] mat) {
        int n = mat.length;
        if (n == 1) return mat[0][0];
        if (n == 2) return mat[0][0]*mat[1][1] - mat[0][1]*mat[1][0];

        double det = 0;
        for (int col=0; col<n; col++){
            double[][] minor = new double[n-1][n-1];
            for (int i=1;i<n;i++){
                int idx=0;
                for (int j=0;j<n;j++){
                    if (j==col) continue;
                    minor[i-1][idx++] = mat[i][j];
                }
            }
            det += ((col%2==0)?1:-1) * mat[0][col] * determinant(minor);
        }
        return det;
    }

    @Override
    public double[] solve(double[][] matrix, double[] vector) throws Exception {
        int n = vector.length;
        double det = determinant(matrix);
        if (Math.abs(det) < 1e-12) throw new Exception("Matriz singular (Cramer)");
        double[] sol = new double[n];
        for (int i=0;i<n;i++){
            double[][] mod = new double[n][n];
            for (int r=0;r<n;r++) for (int c=0;c<n;c++) mod[r][c] = matrix[r][c];
            for (int r=0;r<n;r++) mod[r][i] = vector[r];
            sol[i] = determinant(mod) / det;
        }
        return sol;
    }
}
