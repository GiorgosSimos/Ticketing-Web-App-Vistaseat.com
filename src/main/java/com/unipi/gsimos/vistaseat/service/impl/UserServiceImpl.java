package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.exception.ResourceNotFoundException;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new user in the system.
     * <p>
     * To ensure password confidentiality, this method accepts a {@code NewUserDto} containing the user's credentials,
     * including the password, and returns a {@code UserDto} without the password field.
     *
     * @param newUserDto the data transfer object containing the new user's details
     * @return a {@code UserDto} representing the created user (excluding the password)
     */

    @Override
    public UserDto createUser(NewUserDto newUserDto) {
        if (userRepository.existsByEmail(newUserDto.getEmail())){
            throw new RuntimeException("Email already exists");
        }
        //Before saving, encrypt password with BCryptPasswordEncoder
        newUserDto.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

        //Default userRole -> REGISTERED
        newUserDto.setRole(UserRole.REGISTERED);

        User user = UserMapper.toUser(newUserDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("User with id " + userId + " not found")
                );
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((UserMapper::toUserDto)).collect(Collectors.toList());
    }

    @Override
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").descending());
        Page<User> users = userRepository.findAll(pageable);
        return users.map((UserMapper::toUserDto));
    }

    @Override
    public Page<UserDto> getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").descending());
        Page<User> users = userRepository.findAllByRole(role, pageable);
        return users.map((UserMapper::toUserDto));
    }

    @Override
    public UserDto updateUser(Long userId, UserDto updatedUser) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found")
        );
        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setEmail(updatedUser.getEmail());
        user.setPhone(updatedUser.getPhone());
        user.setRole(updatedUser.getRole());

        User updatedUserObj = userRepository.save(user);
        return UserMapper.toUserDto(updatedUserObj);

    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found")
        );

        userRepository.deleteById(userId);
    }

    @Override
    public long countUsersByRole(UserRole role) {
        return userRepository.countByRole(role);
    }

    /**
     * Retrieves the 10 most recently registered users with the role REGISTERED.
     * <p>
     * This method uses {@link UserRepository#findTop10ByRoleOrderByIdDesc(UserRole)}
     * to fetch the data, assuming higher IDs correspond to newer registrations.
     * It then converts the list of {@link User} entities into a list of {@link UserDto}
     * using the {@link UserMapper#toUserDto(User)} method via Java Stream API.
     * <p>
     * Java Streams provide a declarative and readable approach to transform
     * collections, replacing manual iteration with a functional-style pipeline.
     * @return a list of the 10 most recent registered users as {@link UserDto}
     */
    @Override
    public List<UserDto> getLast10Users() {
        List<User> users = userRepository.findTop10ByRoleOrderByIdDesc(UserRole.REGISTERED);
        return users.stream()
                .map((UserMapper::toUserDto))
                .collect(Collectors.toList());
    }
}
