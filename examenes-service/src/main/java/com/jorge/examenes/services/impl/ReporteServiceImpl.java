package com.jorge.examenes.services.impl;

import com.jorge.examenes.services.ReporteService;
import org.eclipse.birt.report.engine.api.*;
import org.springframework.stereotype.Service;
import com.mysql.cj.jdbc.JdbcConnection;
import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;

@Service
public class ReporteServiceImpl implements ReporteService {

    private final IReportEngine reportEngine;
    private final DataSource dataSource;

    public ReporteServiceImpl(IReportEngine reportEngine, DataSource dataSource) {
        this.reportEngine = reportEngine;
        this.dataSource = dataSource;
    }

    @Override
    public byte[] generarReporteExamen(Long idEvaluacion) throws Exception {
        String rutaArchivo = "/reports/DetalleExamen.rptdesign";
        InputStream designStream = getClass().getResourceAsStream(rutaArchivo);

        if (designStream == null) {
            throw new RuntimeException("Archivo BIRT no encontrado: " + rutaArchivo);
        }

        IReportRunnable design = reportEngine.openReportDesign(designStream);

        // RunAndRenderTask: funciona en una sola pasada cuando el EngineHome
        // está correctamente configurado con los plugins OSGi en disco
        IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);

        try (Connection connection = dataSource.getConnection()) {
            // Parámetro del informe
            task.setParameterValue("id_evaluacion", idEvaluacion.intValue());

            // Inyección de conexión JDBC (evita que BIRT abra una segunda conexión)
            task.getAppContext().put("OdaJDBCDriverPassInConnection", connection);
            task.getAppContext().put(
                    "OdaJDBCDriverPassInConnectionEnabled", Boolean.TRUE
            );

            // Opciones de renderizado PDF
            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat(RenderOption.OUTPUT_FORMAT_PDF);
            options.setOption(IPDFRenderOption.FIT_TO_PAGE, Boolean.TRUE);
            options.setOption(IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY, Boolean.FALSE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            options.setOutputStream(out);

            task.setRenderOption(options);
            task.run();

            return out.toByteArray();

        } finally {
            task.close();
        }
    }

    @Override
    public byte[] generarReporteGeneralExamenes() throws Exception {
        String rutaArchivo = "/reports/ListadoExamenes.rptdesign";
        InputStream designStream = getClass().getResourceAsStream(rutaArchivo);

        if (designStream == null) {
            throw new RuntimeException("Archivo BIRT no encontrado: " + rutaArchivo);
        }

        IReportRunnable design = reportEngine.openReportDesign(designStream);
        IRunAndRenderTask task = reportEngine.createRunAndRenderTask(design);

        try (Connection connection = dataSource.getConnection()) {
            // Inyección de la conexión compartida
            task.getAppContext().put("OdaJDBCDriverPassInConnection", connection);
            task.getAppContext().put("OdaJDBCDriverPassInConnectionEnabled", Boolean.TRUE);

            // 🔥 LA LÍNEA SALVAVIDAS: Prohíbe a BIRT cerrar la conexión tras pintar el gráfico 🔥
            task.getAppContext().put("OdaJDBCDriverPassInConnectionCloseAfterUse", Boolean.FALSE);

            PDFRenderOption options = new PDFRenderOption();
            options.setOutputFormat(RenderOption.OUTPUT_FORMAT_PDF);

            // Paginación libre para que la tabla quepa entera
            options.setOption(IPDFRenderOption.FIT_TO_PAGE, Boolean.FALSE);
            options.setOption(IPDFRenderOption.PAGEBREAK_PAGINATION_ONLY, Boolean.FALSE);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            options.setOutputStream(out);

            task.setRenderOption(options);
            task.run();

            return out.toByteArray();
        } finally {
            task.close();
        }
    }
}