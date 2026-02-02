package hr.unizg.pmf.matrixcalc.ui;

import hr.unizg.pmf.matrixcalc.ui.dto.MatrixDTO;
import hr.unizg.pmf.matrixcalc.ui.model.HistoryItem;
import hr.unizg.pmf.matrixcalc.ui.model.SparseEntry;
import hr.unizg.pmf.matrixcalc.ui.service.FakeMatrixServiceClient;
import hr.unizg.pmf.matrixcalc.ui.service.MatrixServiceClient;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import hr.unizg.pmf.matrixcalc.ui.service.MatrixServiceClientImpl;


import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class MainController {

    @FXML private TextField aRowsField;
    @FXML private TextField aColsField;
    @FXML private TableView<SparseEntry> aTable;

    @FXML private TextField bRowsField;
    @FXML private TextField bColsField;
    @FXML private TableView<SparseEntry> bTable;

    @FXML private TableView<SparseEntry> vecBTable;

    @FXML private ProgressIndicator progress;
    @FXML private Label statusLabel;
    @FXML private TextArea resultArea;

    @FXML private Button btnAdd;
    @FXML private Button btnSub;
    @FXML private Button btnMul;
    @FXML private Button btnSolve;
    @FXML private Button btnTranspose;
    @FXML private Button btnPinv;

    @FXML private ListView<HistoryItem> historyList;

    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final MatrixServiceClient service = new MatrixServiceClientImpl();

    @FXML
    public void initialize() {
        setupEntryTable(aTable);
        setupEntryTable(bTable);
        setupEntryTable(vecBTable);

        aRowsField.setText("3"); aColsField.setText("3");
        bRowsField.setText("3"); bColsField.setText("3");

        historyList.getSelectionModel().selectedItemProperty().addListener((obs, oldV, item) -> {
            if (item != null) loadFromHistory(item);
        });
    }
    
    private void setupEntryTable(TableView<SparseEntry> table) {
        table.setEditable(true);

        var cRow = new TableColumn<SparseEntry, Integer>("row");
        cRow.setCellValueFactory(d -> d.getValue().rowProperty().asObject());
        cRow.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cRow.setOnEditCommit(e -> e.getRowValue().setRow(e.getNewValue()));

        var cCol = new TableColumn<SparseEntry, Integer>("col");
        cCol.setCellValueFactory(d -> d.getValue().colProperty().asObject());
        cCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        cCol.setOnEditCommit(e -> e.getRowValue().setCol(e.getNewValue()));

        var cVal = new TableColumn<SparseEntry, Double>("value");
        cVal.setCellValueFactory(d -> d.getValue().valueProperty().asObject());
        cVal.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        cVal.setOnEditCommit(e -> e.getRowValue().setValue(e.getNewValue()));

        table.getColumns().setAll(cRow, cCol, cVal);
    }

    @FXML public void onAddA() { aTable.getItems().add(new SparseEntry(0, 0, 1.0)); }
    @FXML public void onRemoveA() { removeSelected(aTable); }

    @FXML public void onAddB() { bTable.getItems().add(new SparseEntry(0, 0, 1.0)); }
    @FXML public void onRemoveB() { removeSelected(bTable); }

    @FXML public void onAddVecB() { vecBTable.getItems().add(new SparseEntry(0, 0, 1.0)); }
    @FXML public void onRemoveVecB() { removeSelected(vecBTable); }

    private void removeSelected(TableView<SparseEntry> table) {
        var sel = table.getSelectionModel().getSelectedItem();
        if (sel != null) table.getItems().remove(sel);
    }

    private MatrixDTO toDto(TextField rowsF, TextField colsF, TableView<SparseEntry> table) {
        int rows = parsePositiveInt(rowsF, "rows");
        int cols = parsePositiveInt(colsF, "cols");

        var entries = table.getItems().stream()
                .filter(e -> e.getValue() != 0.0)
                .map(e -> new MatrixDTO.EntryDTO(e.getRow(), e.getCol(), e.getValue()))
                .toList();

        MatrixDTO dto = new MatrixDTO(rows, cols, entries);
        validateEntries(dto, "Matrix");
        return dto;
    }

    private MatrixDTO toVectorDto(int rows, TableView<SparseEntry> table) {
        if (rows <= 0) throw new IllegalArgumentException("A.rows must be > 0 to build vector b.");

        var entries = table.getItems().stream()
                .filter(e -> e.getValue() != 0.0)
                .map(e -> new MatrixDTO.EntryDTO(e.getRow(), 0, e.getValue())) // col je uvijek 0
                .toList();

        MatrixDTO dto = new MatrixDTO(rows, 1, entries);
        validateEntries(dto, "Vector b");
        return dto;
    }

    private int parsePositiveInt(TextField f, String name) {
        String s = f.getText() == null ? "" : f.getText().trim();
        try {
            int x = Integer.parseInt(s);
            if (x <= 0) throw new IllegalArgumentException(name + " must be > 0.");
            return x;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(name + " must be an integer > 0. Got: '" + s + "'");
        }
    }

    private void validateEntries(MatrixDTO dto, String name) {
        for (var e : dto.entries()) {
            if (e.row() < 0 || e.row() >= dto.rows())
                throw new IllegalArgumentException(name + ": row out of bounds: " + e.row());
            if (e.col() < 0 || e.col() >= dto.cols())
                throw new IllegalArgumentException(name + ": col out of bounds: " + e.col());
        }
    }

    private void runAsync(String label, Callable<String> work, Consumer<String> onSuccess) {
        setBusy(true, label);

        Task<String> task = new Task<>() {
            @Override protected String call() throws Exception { return work.call(); }
        };

        task.setOnSucceeded(e -> {
            String res = task.getValue();
            resultArea.setText(res);
            if (onSuccess != null) onSuccess.accept(res);
            setBusy(false, "Ready");
        });

        task.setOnFailed(e -> {
            showError(task.getException());
            setBusy(false, "Error");
        });

        exec.submit(task);
    }

    private void setBusy(boolean busy, String status) {
        progress.setVisible(busy);
        statusLabel.setText(status);

        btnAdd.setDisable(busy);
        btnSub.setDisable(busy);
        btnMul.setDisable(busy);
        btnSolve.setDisable(busy);
        btnTranspose.setDisable(busy);
        btnPinv.setDisable(busy);

        aTable.setDisable(busy);
        bTable.setDisable(busy);
        vecBTable.setDisable(busy);
    }

    private void showError(Throwable ex) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Error");
        a.setHeaderText("Operation failed");
        a.setContentText(ex == null ? "Unknown error" : ex.getMessage());
        a.showAndWait();
    }

    private void pushHistory(String op, MatrixDTO A, MatrixDTO B, MatrixDTO vecB, String resultText) {
        historyList.getItems().add(0, new HistoryItem(op, LocalDateTime.now(), A, B, vecB, resultText));
    }

    private void loadFromHistory(HistoryItem item) {
        resultArea.setText(item.resultText());
        applyDtoToEditor(item.a(), aRowsField, aColsField, aTable);
        if (item.b() != null) applyDtoToEditor(item.b(), bRowsField, bColsField, bTable);
        if (item.vecB() != null) applyDtoToVector(item.vecB(), vecBTable);
    }

    private void applyDtoToEditor(MatrixDTO dto, TextField rowsF, TextField colsF, TableView<SparseEntry> table) {
        rowsF.setText(Integer.toString(dto.rows()));
        colsF.setText(Integer.toString(dto.cols()));
        table.getItems().clear();
        for (var e : dto.entries()) {
            table.getItems().add(new SparseEntry(e.row(), e.col(), e.value()));
        }
    }

    private void applyDtoToVector(MatrixDTO dto, TableView<SparseEntry> table) {
        table.getItems().clear();
        for (var e : dto.entries()) {
            table.getItems().add(new SparseEntry(e.row(), 0, e.value()));
        }
    }

    @FXML public void onAdd() {
        MatrixDTO A, B;
        try {
            A = toDto(aRowsField, aColsField, aTable);
            B = toDto(bRowsField, bColsField, bTable);
            if (A.rows() != B.rows() || A.cols() != B.cols())
                throw new IllegalArgumentException("A and B must have same dimensions for +.");
        } catch (Exception ex) { showError(ex); return; }

        runAsync("Computing A+B...", () -> service.add(A, B),
                res -> pushHistory("A + B", A, B, null, res));
    }

    @FXML public void onSub() {
        MatrixDTO A, B;
        try {
            A = toDto(aRowsField, aColsField, aTable);
            B = toDto(bRowsField, bColsField, bTable);
            if (A.rows() != B.rows() || A.cols() != B.cols())
                throw new IllegalArgumentException("A and B must have same dimensions for -.");
        } catch (Exception ex) { showError(ex); return; }

        runAsync("Computing A-B...", () -> service.sub(A, B),
                res -> pushHistory("A - B", A, B, null, res));
    }

    @FXML public void onMul() {
        MatrixDTO A, B;
        try {
            A = toDto(aRowsField, aColsField, aTable);
            B = toDto(bRowsField, bColsField, bTable);
            if (A.cols() != B.rows())
                throw new IllegalArgumentException("For A·B, A.cols must equal B.rows.");
        } catch (Exception ex) { showError(ex); return; }

        runAsync("Computing A·B...", () -> service.mul(A, B),
                res -> pushHistory("A · B", A, B, null, res));
    }

    @FXML public void onTranspose() {
        MatrixDTO A;
        try { A = toDto(aRowsField, aColsField, aTable); }
        catch (Exception ex) { showError(ex); return; }

        runAsync("Transposing A...", () -> service.transpose(A),
                res -> pushHistory("transpose(A)", A, null, null, res));
    }

    @FXML public void onPinv() {
        MatrixDTO A;
        try { A = toDto(aRowsField, aColsField, aTable); }
        catch (Exception ex) { showError(ex); return; }

        runAsync("Computing pinv(A)...", () -> service.pinv(A),
                res -> pushHistory("pinv(A)", A, null, null, res));
    }

    @FXML public void onSolve() {
        MatrixDTO A, vecB;
        try {
            A = toDto(aRowsField, aColsField, aTable);
            vecB = toVectorDto(A.rows(), vecBTable);
        } catch (Exception ex) { showError(ex); return; }

        runAsync("Solving Ax=b...", () -> service.solve(A, vecB),
                res -> pushHistory("solve(Ax=b)", A, null, vecB, res));
    }
}
