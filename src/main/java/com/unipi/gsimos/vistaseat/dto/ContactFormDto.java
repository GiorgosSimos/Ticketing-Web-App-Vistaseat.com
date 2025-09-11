package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.ContactCategory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactFormDto {

    @NotBlank
    @Size(min = 2, max = 80)
    private String name;

    @NotBlank @Email
    @Size(max = 120)
    private String email;

    @NotBlank @Size(min = 5, max = 120)
    private String subject;

    @Size(max = 50)
    private ContactCategory category;

    @NotBlank @Size(min = 20, max = 1000)
    private String message;

    @AssertTrue
    private boolean consent;

    // honeypot to detect possible spammers
    private String hp;
}
