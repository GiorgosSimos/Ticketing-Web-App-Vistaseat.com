package com.unipi.gsimos.vistaseat.dto;
import com.unipi.gsimos.vistaseat.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {

    private Long id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private UserRole role;
    private boolean active;
    private LocalDateTime registrationDate;
}