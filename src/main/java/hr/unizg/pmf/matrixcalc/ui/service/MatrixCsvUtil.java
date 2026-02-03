package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;
import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO.EntryDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public final class MatrixCsvUtil {

    private MatrixCsvUtil() {}

    // Export MatrixDTO to CSV
    public static void exportToCsv(MatrixDTO matrix, File file) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // First line: rows,cols
            writer.write(matrix.rows() + "," + matrix.cols());
            writer.newLine();

            for (EntryDTO e : matrix.entries()) {
                writer.write(e.row() + "," + e.col() + "," + e.value());
                writer.newLine();
            }
        }
    }

    // Import CSV into MatrixDTO
    public static MatrixDTO importFromCsv(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String firstLine = reader.readLine();
            if (firstLine == null) throw new IOException("Empty CSV file");

            String[] dims = firstLine.split(",");
            int rows = Integer.parseInt(dims[0].trim());
            int cols = Integer.parseInt(dims[1].trim());

            List<EntryDTO> entries = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 3) continue; // ignore malformed lines
                int r = Integer.parseInt(parts[0].trim());
                int c = Integer.parseInt(parts[1].trim());
                double v = Double.parseDouble(parts[2].trim());
                entries.add(new EntryDTO(r, c, v));
            }

            return new MatrixDTO(rows, cols, entries);
        }
    }
}
