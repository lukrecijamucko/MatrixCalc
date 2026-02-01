package hr.unizg.pmf.matrixcalc.ui.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class SparseEntry {
    private final IntegerProperty row = new SimpleIntegerProperty();
    private final IntegerProperty col = new SimpleIntegerProperty();
    private final DoubleProperty value = new SimpleDoubleProperty();

    public SparseEntry(int row, int col, double value) {
        this.row.set(row);
        this.col.set(col);
        this.value.set(value);
    }

    public int getRow() { return row.get(); }
    public void setRow(int r) { row.set(r); }
    public IntegerProperty rowProperty() { return row; }

    public int getCol() { return col.get(); }
    public void setCol(int c) { col.set(c); }
    public IntegerProperty colProperty() { return col; }

    public double getValue() { return value.get(); }
    public void setValue(double v) { value.set(v); }
    public DoubleProperty valueProperty() { return value; }
}
