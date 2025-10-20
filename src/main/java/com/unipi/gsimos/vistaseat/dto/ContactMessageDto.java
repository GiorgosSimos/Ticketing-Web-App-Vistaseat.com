package com.unipi.gsimos.vistaseat.dto;

import com.unipi.gsimos.vistaseat.model.ContactCategory;
import com.unipi.gsimos.vistaseat.model.ContactStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ContactMessageDto {

    private Long id;
    private String authorName;
    private String authorEmail;
    private String subject;
    private ContactCategory category;
    private String message;
    private ContactStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private String adminNotes;
}
