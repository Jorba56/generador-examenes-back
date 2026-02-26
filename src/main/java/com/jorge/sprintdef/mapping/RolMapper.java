package com.jorge.sprintdef.mapping;


import com.jorge.sprintdef.Rol;
import com.jorge.sprintdef.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RolMapper {
    RolDTO mappingADTO(Rol rol);

    RolPutDTO mappingPut(Rol rol);

    Rol mappingPutReverse(RolPutDTO rolNuevo);
}
