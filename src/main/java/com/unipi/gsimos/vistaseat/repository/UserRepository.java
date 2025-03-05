package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
}
