package com.unipi.gsimos.vistaseat.mapper;


import com.unipi.gsimos.vistaseat.dto.ContactMessageDto;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ContactMessageMapper {

    public ContactMessage toEntity (ContactMessageDto contactMessageDto) {
        ContactMessage contactMessage = new ContactMessage();

        contactMessage.setName(contactMessageDto.getAuthorName());
        contactMessage.setEmail(contactMessageDto.getAuthorEmail());
        contactMessage.setSubject(contactMessageDto.getSubject());
        contactMessage.setCategory(contactMessageDto.getCategory());
        contactMessage.setMessage(contactMessageDto.getMessage());
        contactMessage.setStatus(contactMessageDto.getStatus());
        contactMessage.setCreatedAt(contactMessageDto.getCreatedAt());
        contactMessage.setResolvedAt(contactMessageDto.getResolvedAt());
        contactMessage.setAdminNotes(contactMessageDto.getAdminNotes());

        return contactMessage;
    }

    public ContactMessageDto toDto (ContactMessage contactMessage) {

        ContactMessageDto contactMessageDto = new ContactMessageDto();

        contactMessageDto.setId(contactMessage.getId());
        contactMessageDto.setAuthorName(contactMessage.getName());
        contactMessageDto.setAuthorEmail(contactMessage.getEmail());
        contactMessageDto.setSubject(contactMessage.getSubject());
        contactMessageDto.setCategory(contactMessage.getCategory());
        contactMessageDto.setMessage(contactMessage.getMessage());
        contactMessageDto.setStatus(contactMessage.getStatus());
        contactMessageDto.setCreatedAt(contactMessage.getCreatedAt());
        contactMessageDto.setResolvedAt(contactMessage.getResolvedAt());
        contactMessageDto.setAdminNotes(contactMessage.getAdminNotes());

        return contactMessageDto;
    }
}
