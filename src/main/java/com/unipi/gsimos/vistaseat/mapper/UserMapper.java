package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.User;

public class UserMapper {

    // Used only when creating new users
    /*public static NewUserDto toNewUserDto(User user) {
        return new NewUserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPassword(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
                *//*user.getManagedEvents() != null ? user.getManagedEvents()
                        .stream().map(Event::getId).collect(Collectors.toList()) : null,
                user.getManagedVenues() != null ? user.getManagedVenues()
                        .stream().map(Venue::getId).collect(Collectors.toList()) : null*//*
        );
    }*/

    // Used only when creating new users
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

    public static UserDto toUserDto(User user) {
        return new UserDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
        );
    }

    /*public static UserDto toUserDto(NewUserDto newUserDto) {
        UserDto user = new UserDto();
        user.setFirstName(newUserDto.getFirstName());
        user.setLastName(newUserDto.getLastName());
        user.setPhone(newUserDto.getPhone());
        user.setEmail(newUserDto.getEmail());
        user.setRole(newUserDto.getRole());
        return user;
    }*/
}
