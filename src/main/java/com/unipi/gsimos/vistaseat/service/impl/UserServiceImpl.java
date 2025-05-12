package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.exception.ResourceNotFoundException;
import com.unipi.gsimos.vistaseat.mapper.UserMapper;
import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import com.unipi.gsimos.vistaseat.repository.UserRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;
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
    @Transactional
    public void toggleUserStatus(Long userId) throws AccessDeniedException {
        verifyNotSelf(userId, "Action denied: You cannot change status on your own administrator account.");

        User user = userRepository.findById(userId)
                .orElseThrow(()-> new EntityNotFoundException("User with id " + userId + " not found"));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) throws AccessDeniedException {
        verifyNotSelf(userId, "Action denied: You cannot delete your own administrator account.");

        userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found")
        );
        userRepository.deleteById(userId);
    }

    @Override
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map((UserMapper::toUserDto)).collect(Collectors.toList());
    }

    /**
     * Retrieves a paginated and sorted list of all users, mapped to {@link UserDto} objects.
     *
     * <p>This method uses Spring Data pagination by executing a SQL query similar to:
     * <pre>
     * SELECT * FROM users
     * ORDER BY registration_date DESC
     * LIMIT size OFFSET page * size
     * </pre>
     * where:
     * <ul>
     *   <li><b>page</b>: the index of the page to retrieve (starting from 0)</li>
     *   <li><b>size</b>: the number of users per page</li>
     * </ul>
     *
     * <p>The result is sorted in descending order based on the user's registration date.
     * Each {@link User} entity is then converted to a {@link UserDto} to ensure that
     * sensitive fields such as passwords, tokens, or internal IDs are not exposed to the client.
     *
     * @param page the page number to retrieve (zero-based)
     * @param size the number of users per page
     * @return a {@link Page} of {@link UserDto} objects, sorted by registration date in descending order
     */
    @Override
    public Page<UserDto> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").descending());
        Page<User> users = userRepository.findAll(pageable);
        return users.map(UserMapper::toUserDto);
    }

    /**
     * Retrieves a paginated and sorted list of users filtered by their role, mapped to {@link UserDto} objects.
     *
     * <p>This method executes a filtered SQL query similar to:
     * <pre>
     * SELECT * FROM users
     * WHERE role = 'ADMIN'
     * ORDER BY registration_date DESC
     * LIMIT size OFFSET page * size
     * </pre>
     * where:
     * <ul>
     *   <li><b>role</b>: the user role to filter by (e.g., ADMIN, USER)</li>
     *   <li><b>page</b>: the index of the page to retrieve (starting from 0)</li>
     *   <li><b>size</b>: the number of users per page</li>
     * </ul>
     *
     * <p>The results are sorted by registration date in descending order.
     * The returned {@link User} entities are mapped to {@link UserDto} objects
     * to avoid exposing sensitive information (e.g., passwords or internal tokens).
     *
     * <p>This is <b>useful for administrative views where toggling between different user roles</b>
     * (such as administrators and regular users) is required.
     *
     * @param role the {@link UserRole} used to filter users
     * @param page the page number to retrieve (zero-based)
     * @param size the number of users per page
     * @return a {@link Page} of {@link UserDto} objects filtered by role and sorted by registration date
     */
    @Override
    public Page<UserDto> getUsersByRole(UserRole role, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("registrationDate").descending());
        Page<User> users = userRepository.findAllByRole(role, pageable);
        return users.map(UserMapper::toUserDto);
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
    public long countAllUsers() {
        return userRepository.count();
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

    // method to prevent users from performing actions on their own account
    private void verifyNotSelf(Long targetUserId, String errorMessage) throws AccessDeniedException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = auth.getName();
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user not found"));

        if (currentUser.getId().equals(targetUserId)) {
            throw new AccessDeniedException(errorMessage);
        }
    }

}
