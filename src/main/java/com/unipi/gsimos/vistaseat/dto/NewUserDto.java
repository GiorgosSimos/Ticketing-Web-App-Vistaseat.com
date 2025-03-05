package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Used only for creating new users - field password visible
public class NewUserDto {

    private Long Id;
    private String firstName;
    private String lastName;
    private String password;
    private String phone;
    private String email;
    private UserRole role;
}
