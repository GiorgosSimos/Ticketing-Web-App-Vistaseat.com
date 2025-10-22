package com.unipi.gsimos.vistaseat.controller;

import com.unipi.gsimos.vistaseat.dto.NewUserDto;
import com.unipi.gsimos.vistaseat.dto.UserDto;
import com.unipi.gsimos.vistaseat.repository.EventRepository;
import com.unipi.gsimos.vistaseat.repository.VenueRepository;
import com.unipi.gsimos.vistaseat.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserRestController {

    private final UserService userService;
    private final VenueRepository venueRepository;
    private final EventRepository eventRepository;

    @GetMapping("/suggest")
    public List<SuggestionDto> suggest(
                                        @RequestParam String type,
                                        @RequestParam String query) {

        List<SuggestionDto> suggestions;

        if (type.equals("venue")) {
            suggestions = venueRepository.findTop5ByNameContainingIgnoreCase(query)
                    .stream()
                    .map(v -> new SuggestionDto(v.getId(), v.getName(), "/api/venues/" + v.getId()))
                    .toList();
        } else {
            suggestions = eventRepository.findUpcomingByNameGroupBy(query, LocalDateTime.now())
                    .stream()
                    .map(e -> new SuggestionDto(e.getId(), e.getName(), "/api/events/" + e.getId()))
                    .toList();
        }
        return suggestions;
    }

    public record SuggestionDto(Long id, String name, String url) {}

    // Create new user REST API
    @PostMapping("/users/register")
    //@RequestBody annotation is used to extract a JSON object and convert it to a DTO
    public ResponseEntity<UserDto> addNewUser(@RequestBody NewUserDto newUserDto) {

        // Save User
        UserDto savedUser = userService.createUser(newUserDto);
        //return ResponseEntity.ok().build();
        return new ResponseEntity<>(savedUser, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('DOMAIN_ADMIN')")
    @PostMapping("/admin/register")
    public ResponseEntity<UserDto> createAdmin(@RequestBody NewUserDto newUserDto) {

        UserDto savedAdmin = userService.createAdmin(newUserDto);
        return new ResponseEntity<>(savedAdmin, HttpStatus.CREATED);
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
