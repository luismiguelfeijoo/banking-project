package com.ironhack.midterm.repository;

import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecuredUserRepository extends JpaRepository<SecuredUser, Long> {
    SecuredUser findByUsername(String username);
}
