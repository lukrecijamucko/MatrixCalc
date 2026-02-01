package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;

public interface MatrixServiceClient {
    String add(MatrixDTO a, MatrixDTO b);
    String sub(MatrixDTO a, MatrixDTO b);
    String mul(MatrixDTO a, MatrixDTO b);
    String transpose(MatrixDTO a);
    String pinv(MatrixDTO a);
    String solve(MatrixDTO a, MatrixDTO bVector); // bVector je (rows x 1)
}
