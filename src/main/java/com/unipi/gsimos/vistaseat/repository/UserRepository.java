package com.unipi.gsimos.vistaseat.repository;

import com.unipi.gsimos.vistaseat.model.User;
import com.unipi.gsimos.vistaseat.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    long countByRole(UserRole role);

    List<User> findTop10ByRoleOrderByIdDesc(UserRole role);

}
