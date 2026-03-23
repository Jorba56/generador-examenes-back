package com.jorge.examenes.mapping;

import com.jorge.examenes.dto.EvaluacionHistorialDTO;
import com.jorge.examenes.entity.Evaluacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EvaluacionMapper {

    @Mapping(source = "id", target = "idEvaluacion")
    EvaluacionHistorialDTO toHistorialDTO(Evaluacion evaluacion);

    List<EvaluacionHistorialDTO> toHistorialDTOList(List<Evaluacion> evaluaciones);

}