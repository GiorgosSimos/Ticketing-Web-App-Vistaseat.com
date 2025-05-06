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
     * Method to create new user.
     * To not expose the password to the client,
     * the method accepts a NewUserDto object as parameter, but returns a UserDto.
     * @param newUserDto
     * @return UserDto object
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
        return users.stream().map((user -> UserMapper.toUserDto(user))).collect(Collectors.toList());
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

    @Override
    public List<User> getLast10Users() {
        return userRepository.findTop10ByRoleOrderByIdDesc(UserRole.REGISTERED);
    }
}
