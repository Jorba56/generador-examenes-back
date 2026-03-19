package com.jorge.examenes.mapping;

import com.jorge.examenes.dto.PreguntaExamenDTO;
import com.jorge.examenes.entity.Pregunta;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PreguntaMapper {

    PreguntaExamenDTO toDTO(Pregunta pregunta);

    Pregunta toEntity(PreguntaExamenDTO preguntaDTO);

    List<PreguntaExamenDTO> toDTOList(List<Pregunta> preguntas);
}