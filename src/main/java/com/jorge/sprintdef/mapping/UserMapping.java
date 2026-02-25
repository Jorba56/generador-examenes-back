package com.jorge.sprintdef.mapping;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapping {

    UsersAllDTO mappingADTO(User usuario);

    UserIdDTo userToIdDTO (User usuario);

}
