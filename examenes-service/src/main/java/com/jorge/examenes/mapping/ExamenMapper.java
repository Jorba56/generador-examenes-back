package com.jorge.examenes.mapping;

import com.jorge.examenes.dto.ExamenDetalleDTO;
import com.jorge.examenes.dto.ExamenGetDTO;
import com.jorge.examenes.dto.PreguntaExamenDTO;
import com.jorge.examenes.entity.Examen;
import com.jorge.examenes.entity.Pregunta;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExamenMapper {

    //mapeos para listados ligeros
    @Mapping(target = "numeroPreguntas", expression = "java(examen.getPreguntas() != null ? examen.getPreguntas().size() : 0)")
    // calculo para el numero de preguntas (expression)
    ExamenGetDTO toResumenDTO(Examen examen);

    List<ExamenGetDTO> toResumenDTOList(List<Examen> examenes);

    //mapeos para examen completo
    @Mapping(target = "preguntas", source = "preguntas")
    ExamenDetalleDTO toDetalleDTO(Examen examen);

    // al mapear una pregunta al dto del examen, ignoramos el número porque lo calculamos luego
    @Mapping(target = "numero", ignore = true)
    PreguntaExamenDTO toPreguntaExamenDTO(Pregunta pregunta);

    // numeración automática
    @AfterMapping
    default void asignarNumerosDePregunta(@MappingTarget ExamenDetalleDTO dto) {
        if (dto.getPreguntas() != null) {
            for (int i = 0; i < dto.getPreguntas().size(); i++) {
                // asigna el número visual de la pregunta (1, 2, 3...)
                dto.getPreguntas().get(i).setNumero(i + 1);
            }
        }
    }
}