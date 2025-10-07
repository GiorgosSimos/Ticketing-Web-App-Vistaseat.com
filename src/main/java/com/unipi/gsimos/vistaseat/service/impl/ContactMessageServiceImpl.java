package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.dto.ContactMessageDto;
import com.unipi.gsimos.vistaseat.mapper.ContactMessageMapper;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import com.unipi.gsimos.vistaseat.model.ContactStatus;
import com.unipi.gsimos.vistaseat.repository.ContactMessageRepository;
import com.unipi.gsimos.vistaseat.service.ContactMessageService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;
    private final ContactMessageMapper contactMessageMapper;


    @Override
    @Transactional
    public void createContactMessage(ContactMessage contactMessage) {
        // Add validation checks
        contactMessageRepository.save(contactMessage);

    }

    @Override
    public void resolveContactMessage(Long contactMessageId, String adminNotes) {

        ContactMessage contactMessage = contactMessageRepository
                .findById(contactMessageId)
                .orElseThrow(() -> new EntityNotFoundException("Contact message with id: " + contactMessageId + " not found"));

        if (contactMessage.getStatus() == ContactStatus.RESOLVED) {
            throw new IllegalStateException("Contact message status is already RESOLVED");
        } else {
            contactMessage.setStatus(ContactStatus.RESOLVED);
            contactMessage.setResolvedAt(LocalDateTime.now());
            contactMessage.setAdminNotes(adminNotes.trim());
            contactMessageRepository.save(contactMessage);
        }
    }

    @Override
    @Transactional
    public void deleteContactMessage(Long contactMessageId) {

        ContactMessage contactMessage = contactMessageRepository
                .findById(contactMessageId)
                .orElseThrow(()-> new EntityNotFoundException("Contact message with id: " + contactMessageId + " not found"));

        // Perform check to not delete unresolved contact messages
        if (contactMessage.getStatus() == ContactStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot delete a contact message that's not resolved yet!");
        } else {
            contactMessageRepository.delete(contactMessage);
        }

    }

    @Override
    public Page<ContactMessageDto> getAllContactMessages(Pageable pageable) {

        Page<ContactMessage> contactMessages = contactMessageRepository.findAll(pageable);
        List<ContactMessageDto> contactMessageDtos = contactMessages.stream().map(contactMessageMapper::toDto).toList();
        return new PageImpl<>(contactMessageDtos, pageable, contactMessages.getTotalElements());
    }

    @Override
    public Page<ContactMessageDto> getContactMessagesByAuthor(String author, Pageable pageable) {

        Page<ContactMessage> contactMessages = contactMessageRepository.findContactMessageByNameContainingIgnoreCase(author, pageable);
        List<ContactMessageDto> contactMessageDtos = contactMessages.stream().map(contactMessageMapper::toDto).toList();
        return new PageImpl<>(contactMessageDtos, pageable, contactMessages.getTotalElements());

    }
}
