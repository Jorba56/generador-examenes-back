package com.jorge.examenes.utils;

import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.util.List;

public class EvaluacionExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<EvaluacionHistorialDTO> listaNotas;

    public EvaluacionExcelExporter(List<EvaluacionHistorialDTO> listaNotas) {
        this.listaNotas = listaNotas;
        workbook = new XSSFWorkbook();
    }

    private void escribirCabecera() {
        sheet = workbook.createSheet("Notas del Examen");
        Row fila = sheet.createRow(0);

        CellStyle estilo = workbook.createCellStyle();
        XSSFFont fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeight(14);
        estilo.setFont(fuente);

        crearCelda(fila, 0, "Correo del Alumno", estilo);
        crearCelda(fila, 1, "Nota Obtenida", estilo);
        // Si tu DTO tiene la fecha, puedes añadirla aquí como tercera columna
    }

    private void crearCelda(Row fila, int contadorColumnas, Object valor, CellStyle estilo) {
        sheet.autoSizeColumn(contadorColumnas);
        Cell celda = fila.createCell(contadorColumnas);

        if (valor instanceof Double) {
            celda.setCellValue((Double) valor);
        } else if (valor instanceof Long) {
            celda.setCellValue((Long) valor);
        } else if (valor != null) {
            celda.setCellValue(valor.toString());
        }
        celda.setCellStyle(estilo);
    }

    private void escribirDatos() {
        int contadorFilas = 1;
        CellStyle estilo = workbook.createCellStyle();
        XSSFFont fuente = workbook.createFont();
        fuente.setFontHeight(12);
        estilo.setFont(fuente);

        for (EvaluacionHistorialDTO evaluacion : listaNotas) {
            Row fila = sheet.createRow(contadorFilas++);
            int contadorColumnas = 0;
            // Ajusta estos getters a los nombres reales que tengas en tu EvaluacionHistorialDTO
            crearCelda(fila, contadorColumnas++, evaluacion.getCorreoUsuario(), estilo);
            crearCelda(fila, contadorColumnas++, evaluacion.getNota(), estilo);
        }
    }

    public void exportar(HttpServletResponse response) throws IOException {
        escribirCabecera();
        escribirDatos();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();
        outputStream.close();
    }
}