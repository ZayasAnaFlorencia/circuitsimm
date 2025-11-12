package com.zayas.circuit.solver;

public class GaussJordanSolver implements SolverStrategy {

    @Override
    public double[] solve(double[][] matrix, double[] vector) throws Exception {
        int n = vector.length;
        double[][] aug = new double[n][n+1];
        for (int i=0;i<n;i++){
            for (int j=0;j<n;j++) aug[i][j] = matrix[i][j];
            aug[i][n] = vector[i];
        }

        for (int i=0;i<n;i++){
            int max = i;
            for (int r=i+1;r<n;r++){
                if (Math.abs(aug[r][i]) > Math.abs(aug[max][i])) max = r;
            }
            double[] tmp = aug[i]; aug[i] = aug[max]; aug[max] = tmp;
            double pivot = aug[i][i];
            if (Math.abs(pivot) < 1e-12) throw new Exception("Matriz singular (Gauss-Jordan)");
            for (int j=i;j<=n;j++) aug[i][j] /= pivot;
            for (int r=0;r<n;r++){
                if (r==i) continue;
                double factor = aug[r][i];
                for (int c=i;c<=n;c++) aug[r][c] -= factor * aug[i][c];
            }
        }

        double[] sol = new double[n];
        for (int i=0;i<n;i++) sol[i] = aug[i][n];
        return sol;
    }
}
