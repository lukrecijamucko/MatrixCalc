package hr.unizg.pmf.matrixcalc.ui.model;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;

import java.time.LocalDateTime;

public record HistoryItem(
        String op,
        LocalDateTime at,
        MatrixDTO a,
        MatrixDTO b,       // može biti null
        MatrixDTO vecB,    // može biti null
        String resultText
) {
    @Override
    public String toString() {
        return op + "  (" + at.toLocalTime().withNano(0) + ")";
    }
}