package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("api/users")
public class UserController {

    private final UserService userService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(UserService userService, BCryptPasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    // Create new user REST API
    @PostMapping("/register")
    public ResponseEntity<NewUserDto> addNewUser(@RequestBody NewUserDto newUserDto) {
        //Before saving, encrypt password with BCryptPasswordEncoder
        newUserDto.setPassword(passwordEncoder.encode(newUserDto.getPassword()));

        // Save User
        NewUserDto savedUser = userService.createUser(newUserDto);
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }
}
