package com.unipi.gsimos.vistaseat.service.impl;

import com.unipi.gsimos.vistaseat.model.ContactMessage;
import com.unipi.gsimos.vistaseat.repository.ContactMessageRepository;
import com.unipi.gsimos.vistaseat.service.ContactMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContactMessageServiceImpl implements ContactMessageService {

    private final ContactMessageRepository contactMessageRepository;

    @Override
    public void createContactMessage(ContactMessage contactMessage) {
        // Add validation checks
        contactMessageRepository.save(contactMessage);

    }
}
