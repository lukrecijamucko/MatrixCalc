/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hr.unizg.pmf.matrixcalc.ui.dto;
import java.util.List;
/**
 *
 * @author Korisnik
 */

public record MatrixDTO(int rows, int cols, List<EntryDTO> entries) {
    public record EntryDTO(int row, int col, double value) {}
}
