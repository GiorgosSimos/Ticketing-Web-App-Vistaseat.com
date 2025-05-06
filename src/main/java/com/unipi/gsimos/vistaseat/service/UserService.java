package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.User;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserDto newUserDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long userId, UserDto updatedUser);

    void deleteUser(Long userId);

    long countUsers();

    List<User> getLast10Users();
}
