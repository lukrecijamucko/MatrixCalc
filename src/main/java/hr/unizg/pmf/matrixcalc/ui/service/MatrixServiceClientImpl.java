package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;

import java.util.Map;

public class MatrixServiceClientImpl implements MatrixServiceClient {
    
    public native MatrixDTO matMul(MatrixDTO a,MatrixDTO b);
    
    public native MatrixDTO matSolve(MatrixDTO a,MatrixDTO b);
    
    public native MatrixDTO matPinv(MatrixDTO a);
    
    static {
        System.loadLibrary("MatrixOps");
    }

    @Override
    public String add(MatrixDTO a, MatrixDTO b) {
        var A = MatrixMapper.toSparseMap(a);
        var B = MatrixMapper.toSparseMap(b);

        for (var e : B.entrySet()) {
            A.merge(e.getKey(), e.getValue(), Double::sum);
            if (Math.abs(A.get(e.getKey())) < 1e-12) {
                A.remove(e.getKey());
            }
        }

        return "A + B\n" + MatrixMapper.formatSparseMatrix(a.rows(), a.cols(), A);
    }

    @Override
    public String sub(MatrixDTO a, MatrixDTO b) {
        var A = MatrixMapper.toSparseMap(a);
        var B = MatrixMapper.toSparseMap(b);

        for (var e : B.entrySet()) {
            A.merge(e.getKey(), -e.getValue(), Double::sum);
            if (Math.abs(A.get(e.getKey())) < 1e-12) {
                A.remove(e.getKey());
            }
        }

        return "A - B\n" + MatrixMapper.formatSparseMatrix(a.rows(), a.cols(), A);
    }

    @Override
    public String mul(MatrixDTO a, MatrixDTO b) {
        // placeholder until math teammate plugs real multiplication
        var c = matMul(a, b);
        var C = MatrixMapper.toSparseMap(c);
        return "A · B\n" + MatrixMapper.formatSparseMatrix(c.rows(), c.cols(), C);
    }

    @Override
    public String transpose(MatrixDTO a) {
        var A = MatrixMapper.toSparseMap(a);
        int rows = a.rows();
        int cols = a.cols();

        Map<Integer, Double> T = new java.util.HashMap<>();

        for (var e : A.entrySet()) {
            int key = e.getKey();
            int r = key / cols;
            int c = key % cols;

            int newKey = c * rows + r;
            T.put(newKey, e.getValue());
        }

        return "transpose(A)\n" +
                MatrixMapper.formatSparseMatrix(cols, rows, T);
    }

    @Override
    public String pinv(MatrixDTO a) {
        return "pinv(A)\n(pseudoinverse computed in math layer)";
    }

    @Override
    public String solve(MatrixDTO a, MatrixDTO bVector) {
        return "solve(Ax=b)\n(solution computed in math layer)";
    }
}
