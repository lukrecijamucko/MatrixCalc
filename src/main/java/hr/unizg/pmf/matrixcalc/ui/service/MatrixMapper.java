package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;
import hr.unizg.pmf.matrixcalc.ui.model.SparseEntry;

import java.util.HashMap;
import java.util.Map;

public final class MatrixMapper {

    private MatrixMapper() {}

    /**
     * Converts MatrixDTO to a simple sparse map representation.
     * Key = row * cols + col
     */
    public static Map<Integer, Double> toSparseMap(MatrixDTO dto) {
        Map<Integer, Double> map = new HashMap<>();
        int cols = dto.cols();

        for (MatrixDTO.EntryDTO e : dto.entries()) {
            int key = e.row() * cols + e.col();
            map.put(key, e.value());
        }
        return map;
    }

    public static String formatSparseMatrix(
            int rows, int cols, Map<Integer, Double> data) {

        StringBuilder sb = new StringBuilder();
        sb.append("Matrix ").append(rows).append(" x ").append(cols).append("\n");

        if (data.isEmpty()) {
            sb.append("(all zeros)\n");
            return sb.toString();
        }

        for (var entry : data.entrySet()) {
            int key = entry.getKey();
            int r = key / cols;
            int c = key % cols;
            sb.append("(").append(r).append(", ")
              .append(c).append(") = ")
              .append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
