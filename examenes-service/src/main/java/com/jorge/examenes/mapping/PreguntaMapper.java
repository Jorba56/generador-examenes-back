package com.jorge.examenes.mapping;

import com.jorge.examenes.dto.PreguntaExamenDTO;
import com.jorge.examenes.entity.Pregunta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PreguntaMapper {

    // Ignoramos "numero" porque se calcula dinámicamente o no viene de la base de datos
    @Mapping(target = "numero", ignore = true)
    PreguntaExamenDTO toDTO(Pregunta pregunta);

    List<PreguntaExamenDTO> toDTOList(List<Pregunta> preguntas);

    // Ignoramos los campos internos de la base de datos al convertir de vuelta a entidad
    @Mapping(target = "examenes", ignore = true)
    @Mapping(target = "correcta", ignore = true)
    Pregunta toEntity(PreguntaExamenDTO dto);
}