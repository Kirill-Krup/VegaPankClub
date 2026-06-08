package com.actisys.authservice.repository;

import com.actisys.authservice.model.Credential;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Credential, Long> {
    boolean existsByLogin(String login);

    Credential findByLogin(String login);
}
