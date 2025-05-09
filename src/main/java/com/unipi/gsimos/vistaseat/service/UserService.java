package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.UserRole;
import org.springframework.data.domain.Page;

import java.util.List;

public interface UserService {

    UserDto createUser(NewUserDto newUserDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    Page<UserDto> getAllUsers(int page, int size);

    Page<UserDto> getUsersByRole(UserRole role, int page, int size);

    UserDto updateUser(Long userId, UserDto updatedUser);

    void deleteUser(Long userId);

    long countUsersByRole(UserRole userRole);

    List<UserDto> getLast10Users();
}
