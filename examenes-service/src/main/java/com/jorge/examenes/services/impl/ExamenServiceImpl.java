package com.jorge.examenes.services.impl;

import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import com.jorge.examenes.repository.ExamenRepository;
import com.jorge.examenes.repository.PreguntaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExamenServiceImpl {

    private final ExamenRepository examenRepository;
    private final PreguntaRepository preguntaRepository;

    public ExamenServiceImpl(ExamenRepository examenRepository, PreguntaRepository preguntaRepository) {
        this.examenRepository = examenRepository;
        this.preguntaRepository = preguntaRepository;
    }

    /**
     * Crea un examen automáticamente con N preguntas aleatorias.
     */
    public Examen generarExamenAleatorio(String titulo, String descripcion, int numPreguntas) {
        Examen examen = new Examen();
        examen.setTitulo(titulo);
        examen.setDescripcion(descripcion);

        // 1. Pedimos a la base de datos las N preguntas aleatorias
        List<Pregunta> preguntasAleatorias = preguntaRepository.findPreguntasAleatorias(numPreguntas);

        // 2. Comprobación de seguridad (por si piden 50 y solo hay 10 creadas)
        if (preguntasAleatorias.size() < numPreguntas) {
            throw new RuntimeException("No hay suficientes preguntas en la base de datos para generar este examen.");
        }

        // 3. Vinculamos las preguntas al examen
        examen.setPreguntas(preguntasAleatorias);

        // 4. Guardamos el examen (JPA guardará automáticamente las relaciones en la tabla intermedia)
        return examenRepository.save(examen);
    }
}
