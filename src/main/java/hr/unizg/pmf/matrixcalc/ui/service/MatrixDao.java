package hr.unizg.pmf.matrixcalc.ui.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;
import hr.unizg.pmf.matrixcalc.ui.model.HistoryItem;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MatrixDao {

    private static final String DB_FILE = "matrixcalc.db";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Connection conn;

    public MatrixDao() throws SQLException {
        File db = new File(DB_FILE);
        conn = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
        createTablesIfNotExist();
    }

    private void createTablesIfNotExist() throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS matrices (
                    name TEXT PRIMARY KEY,
                    rows INTEGER,
                    cols INTEGER,
                    entries TEXT
                )
                """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    timestamp TEXT,
                    operation TEXT,
                    a TEXT,
                    b TEXT,
                    vecB TEXT,
                    result TEXT
                )
                """);
        }
    }


    public void saveMatrix(String name, MatrixDTO matrix) throws SQLException {
        String json = toJson(matrix);
        String sql = """
            INSERT INTO matrices(name, rows, cols, entries)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(name) DO UPDATE SET
                rows=excluded.rows,
                cols=excluded.cols,
                entries=excluded.entries
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, matrix.rows());
            ps.setInt(3, matrix.cols());
            ps.setString(4, json);
            ps.executeUpdate();
        }
    }

    public MatrixDTO loadMatrix(String name) throws SQLException {
        String sql = "SELECT rows, cols, entries FROM matrices WHERE name=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int rows = rs.getInt("rows");
                int cols = rs.getInt("cols");
                String entriesJson = rs.getString("entries");
                MatrixDTO dto = fromJson(entriesJson, rows, cols);
                return dto;
            } else return null;
        }
    }


    public void saveHistory(String operation, MatrixDTO A, MatrixDTO B, MatrixDTO vecB, String result) throws SQLException {
        String sql = """
            INSERT INTO history(timestamp, operation, a, b, vecB, result)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDateTime.now().toString());
            ps.setString(2, operation);
            ps.setString(3, A != null ? toJson(A) : null);
            ps.setString(4, B != null ? toJson(B) : null);
            ps.setString(5, vecB != null ? toJson(vecB) : null);
            ps.setString(6, result);
            ps.executeUpdate();
        }
    }

    public List<HistoryItem> loadHistory() throws SQLException {
        List<HistoryItem> list = new ArrayList<>();
        String sql = "SELECT timestamp, operation, a, b, vecB, result FROM history ORDER BY id DESC";
        try (Statement st = conn.createStatement()) {
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                String ts = rs.getString("timestamp");
                String op = rs.getString("operation");
                String aJson = rs.getString("a");
                String bJson = rs.getString("b");
                String vecBJson = rs.getString("vecB");
                String res = rs.getString("result");

                MatrixDTO A = aJson != null ? fromJson(aJson) : null;
                MatrixDTO B = bJson != null ? fromJson(bJson) : null;
                MatrixDTO vecB = vecBJson != null ? fromJson(vecBJson) : null;

                HistoryItem item = new HistoryItem(op, LocalDateTime.parse(ts), A, B, vecB, res);
                list.add(item);
            }
        }
        return list;
    }


    private static String toJson(MatrixDTO dto) {
        try {
            return MAPPER.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to convert MatrixDTO to JSON", e);
        }
    }

    private static MatrixDTO fromJson(String json) {
        try {
            return MAPPER.readValue(json, MatrixDTO.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to parse MatrixDTO from JSON", e);
        }
    }

    private static MatrixDTO fromJson(String json, int rows, int cols) {
        MatrixDTO temp = fromJson(json);
        // Ensure rows/cols match
        return new MatrixDTO(rows, cols, temp.entries());
    }

    public void close() throws SQLException {
        if (conn != null) conn.close();
    }
}
