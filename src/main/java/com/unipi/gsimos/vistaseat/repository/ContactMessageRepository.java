package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}
