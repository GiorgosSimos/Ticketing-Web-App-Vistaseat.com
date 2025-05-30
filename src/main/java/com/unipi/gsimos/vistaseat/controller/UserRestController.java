package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;


@RestController
@RequestMapping("api/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // Create new user REST API
    @PostMapping("/register")
    //@RequestBody annotation is used to extract a JSON object and convert it to a DTO
    public ResponseEntity<UserDto> addNewUser(@RequestBody NewUserDto newUserDto) {

        // Save User
        UserDto savedUser = userService.createUser(newUserDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    // Get user By Id REST API
    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @GetMapping("{id}")
    //@PathVariable is used to bind method parameter userId with URL template variable id
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") Long userId) {
        UserDto userDto = userService.getUserById(userId);
        return ResponseEntity.ok(userDto);
    }

    // Get All users REST API
    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Update user REST API
    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @PutMapping("{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable("id") Long userId,
                                              @RequestBody UserDto updatedUser) {
        UserDto userDto = userService.updateUser(userId, updatedUser);
        return ResponseEntity.ok(userDto);
    }

    // Delete user REST API
    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteUser(@PathVariable("id") Long userId) throws AccessDeniedException {
        userService.deleteUser(userId);
        return ResponseEntity.ok("User with id " + userId + " was deleted successfully");
    }
}
