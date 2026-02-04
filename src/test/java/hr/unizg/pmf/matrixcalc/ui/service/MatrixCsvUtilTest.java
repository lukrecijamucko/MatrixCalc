package hr.unizg.pmf.matrixcalc.ui.service;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;
import hr.unizg.pmf.matrixcalc.ui.model.SparseEntry;
import org.junit.jupiter.api.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatrixCsvUtilTest {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");

    @Test
    void testSaveAndLoadMatrixA() throws Exception {
        // Create a small test matrix
        List<SparseEntry> entries = List.of(
                new SparseEntry(0, 0, 1.0),
                new SparseEntry(1, 2, 2.5)
        );
        MatrixDTO dto = new MatrixDTO(3, 3, entries.stream()
                .map(e -> new MatrixDTO.EntryDTO(e.getRow(), e.getCol(), e.getValue()))
                .toList()
        );

        File file = Path.of(TEMP_DIR, "test_matrixA.csv").toFile();

        // Save
        MatrixCsvUtil.exportToCsv(dto, file);

        // Load
        MatrixDTO loaded = MatrixCsvUtil.importFromCsv(file);

        // Verify size
        assertEquals(dto.rows(), loaded.rows());
        assertEquals(dto.cols(), loaded.cols());

        // Verify entries
        assertEquals(dto.entries().size(), loaded.entries().size());
        for (int i = 0; i < dto.entries().size(); i++) {
            var e1 = dto.entries().get(i);
            var e2 = loaded.entries().get(i);
            assertEquals(e1.row(), e2.row());
            assertEquals(e1.col(), e2.col());
            assertEquals(e1.value(), e2.value(), 1e-9);
        }

        // Cleanup
        Files.deleteIfExists(file.toPath());
    }

    @Test
    void testSaveAndLoadEmptyMatrix() throws Exception {
        MatrixDTO empty = new MatrixDTO(2, 2, List.of());
        File file = Path.of(TEMP_DIR, "empty_matrix.csv").toFile();

        MatrixCsvUtil.exportToCsv(empty, file);
        MatrixDTO loaded = MatrixCsvUtil.importFromCsv(file);

        assertEquals(empty.rows(), loaded.rows());
        assertEquals(empty.cols(), loaded.cols());
        assertTrue(loaded.entries().isEmpty());

        Files.deleteIfExists(file.toPath());
    }
}
