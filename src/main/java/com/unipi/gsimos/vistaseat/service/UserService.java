package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.model.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface UserService {

    UserDto createUser(NewUserDto newUserDto);

    UserDto createAdmin(NewUserDto newUserDto);

    UserDto getUserById(Long userId);

    List<UserDto> getAllUsers();

    Page<UserDto> getAllUsers(int page, int size);

    Page<UserDto> getUsersByRole(UserRole role, int page, int size);

    Page<UserDto> searchUsersByName(String searchQuery, Pageable pageable);

    Page<UserDto> searchUsersByNameAndRole(String searchQuery, UserRole role, Pageable pageable);

    UserDto updateUser(Long userId, UserDto updatedUser);

    void deleteUser(Long userId) throws AccessDeniedException;

    long countUsersByRole(UserRole userRole);

    long countAllUsers();

    List<UserDto> getLast10Users();

    void toggleUserStatus(Long userId) throws AccessDeniedException;
}
