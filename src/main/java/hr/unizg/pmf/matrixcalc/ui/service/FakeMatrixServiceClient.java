package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;

public class FakeMatrixServiceClient implements MatrixServiceClient {

    private static void slow() {
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}
    }

    @Override public String add(MatrixDTO a, MatrixDTO b) {
        slow();
        return "FAKE A + B\nA nnz=" + a.entries().size() + "\nB nnz=" + b.entries().size();
    }

    @Override public String sub(MatrixDTO a, MatrixDTO b) {
        slow();
        return "FAKE A - B";
    }

    @Override public String mul(MatrixDTO a, MatrixDTO b) {
        slow();
        return "FAKE A · B";
    }

    @Override public String transpose(MatrixDTO a) {
        slow();
        return "FAKE transpose(A)";
    }

    @Override public String pinv(MatrixDTO a) {
        slow();
        return "FAKE pinv(A)";
    }

    @Override public String solve(MatrixDTO a, MatrixDTO bVector) {
        slow();
        return "FAKE solve Ax=b\nA nnz=" + a.entries().size() + "\nb nnz=" + bVector.entries().size();
    }
}
