package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.model.User;

public class UserMapper {

    public static NewUserDto toNewUserDto(User user) {

        return new NewUserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
                /*user.getManagedEvents() != null ? user.getManagedEvents()
                        .stream().map(Event::getId).collect(Collectors.toList()) : null,
                user.getManagedVenues() != null ? user.getManagedVenues()
                        .stream().map(Venue::getId).collect(Collectors.toList()) : null*/
        );
    }

    public static User toUser(NewUserDto newUserDto) {
        User user = new User();
        user.setFirstName(newUserDto.getFirstName());
        user.setLastName(newUserDto.getLastName());
        user.setPassword(newUserDto.getPassword()); // Will be hashed before saving
        user.setPhone(newUserDto.getPhone());
        user.setEmail(newUserDto.getEmail());
        user.setRole(newUserDto.getRole());
        return user;
    }

}
