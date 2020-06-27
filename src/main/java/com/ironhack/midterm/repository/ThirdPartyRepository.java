package com.ironhack.midterm.repository;

import com.ironhack.midterm.model.ThirdParty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ThirdPartyRepository extends JpaRepository<ThirdParty, Long> {
    public Optional<ThirdParty> findByHashedKey(UUID hashKey);
}
