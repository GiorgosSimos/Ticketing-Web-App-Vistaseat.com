package com.unipi.gsimos.vistaseat.service;

import com.unipi.gsimos.vistaseat.dto.ContactMessageDto;
import com.unipi.gsimos.vistaseat.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContactMessageService {

    void createContactMessage(ContactMessage contactMessage);

    void deleteContactMessage(Long contactMessageId);

    void resolveContactMessage(Long contactMessageId, String adminNotes);

    Page<ContactMessageDto> getAllContactMessages(Pageable pageable);
}
