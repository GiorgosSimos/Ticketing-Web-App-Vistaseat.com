package com.unipi.gsimos.vistaseat.mapper;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.User;

public class UserMapper {

    // Used only when creating new users
    public static User toUser(NewUserDto newUserDto) {
        User user = new User();
        user.setFirstName(newUserDto.getFirstName());
        user.setLastName(newUserDto.getLastName());
        user.setPassword(newUserDto.getPassword()); // Is hashed in the Service, Layer before saving
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
                user.getRole(),
                user.isActive(),
                user.getRegistrationDate()
        );
    }
}
