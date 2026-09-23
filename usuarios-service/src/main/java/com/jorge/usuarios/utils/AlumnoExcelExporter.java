package com.jorge.usuarios.utils;

import com.jorge.usuarios.dto.AlumnoDTO;
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

/**
 * Clase utilitaria encargada de generar y exportar listados de datos a formato Excel (.xlsx).
 * <p>
 * Utiliza la librería Apache POI para construir dinámicamente el documento en memoria,
 * aplicando estilos a las cabeceras y ajustando el ancho de las columnas antes de
 * inyectar el archivo directamente en el flujo de respuesta HTTP.
 * </p>
 */
public class AlumnoExcelExporter {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<AlumnoDTO> listaAlumnos;

    public AlumnoExcelExporter(List<AlumnoDTO> listaAlumnos) {
        this.listaAlumnos = listaAlumnos;
        // XSSFWorkbook es el formato para archivos .xlsx modernos
        workbook = new XSSFWorkbook();
    }

    private void escribirCabecera() {
        sheet = workbook.createSheet("Alumnos");
        Row fila = sheet.createRow(0);

        CellStyle estilo = workbook.createCellStyle();
        XSSFFont fuente = workbook.createFont();
        fuente.setBold(true);
        fuente.setFontHeight(14);
        estilo.setFont(fuente);

        crearCelda(fila, 0, "ID", estilo);
        crearCelda(fila, 1, "Nombre", estilo);
        crearCelda(fila, 2, "Apellidos", estilo);
        crearCelda(fila, 3, "Correo", estilo);
    }

    private void crearCelda(Row fila, int contadorColumnas, Object valor, CellStyle estilo) {
        sheet.autoSizeColumn(contadorColumnas);
        Cell celda = fila.createCell(contadorColumnas);

        switch (valor) {
            case Long l -> celda.setCellValue((long) valor);
            case Integer i -> celda.setCellValue(i);
            case Boolean b -> celda.setCellValue(b);
            case null, default -> celda.setCellValue((String) valor);
        }
        celda.setCellStyle(estilo);
    }

    private void escribirDatos() {
        int contadorFilas = 1;
        CellStyle estilo = workbook.createCellStyle();
        XSSFFont fuente = workbook.createFont();
        fuente.setFontHeight(12);
        estilo.setFont(fuente);

        for (AlumnoDTO alumno : listaAlumnos) {
            Row fila = sheet.createRow(contadorFilas++);
            int contadorColumnas = 0;
            crearCelda(fila, contadorColumnas++, alumno.getId(), estilo);
            crearCelda(fila, contadorColumnas++, alumno.getNombre(), estilo);
            crearCelda(fila, contadorColumnas++, alumno.getApellidos(), estilo);
            crearCelda(fila, contadorColumnas, alumno.getCorreo(), estilo);
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