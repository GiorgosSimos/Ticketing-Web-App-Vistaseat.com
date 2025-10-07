package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.ContactMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    Page<ContactMessage> findContactMessageByNameContainingIgnoreCase(String authorName, Pageable pageable);
}
