package com.jorge.sprintdef.mapping;

import com.jorge.sprintdef.User;
import com.jorge.sprintdef.dto.UserAddDTO;
import com.jorge.sprintdef.dto.UserByRol;
import com.jorge.sprintdef.dto.UserIdDTo;
import com.jorge.sprintdef.dto.UsersAllDTO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    UsersAllDTO mappingADTO(User usuario);

    UserIdDTo userToIdDTO(User usuario);

    User userAddDTO(UserAddDTO usuario);

    UserByRol mappingRoles(User usuario);
}
