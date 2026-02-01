package hr.unizg.pmf.matrixcalc.ui.model;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record HistoryItem(
        String op,
        LocalDateTime at,
        MatrixDTO a,
        MatrixDTO b,       // može biti null
        MatrixDTO vecB,    // može biti null
        String resultText
) {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    public String toString() {
        return op + "  (" + at.format(FMT) + ")";
    }
}
