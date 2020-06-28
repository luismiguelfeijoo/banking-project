package com.ironhack.midterm.model;

import org.hibernate.annotations.Type;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Entity
public class ThirdParty extends User {

    @NotNull
    @Column(unique = true)
    @Type(type = "uuid-char")
    // v4 UUID, the most secured of the easiest ways to create a random UUID
    private UUID hashedKey;

    public ThirdParty() {
    }

    public ThirdParty(@NotNull @NotEmpty String username, @NotNull @NotEmpty String name) {
        super(username, name);
        this.hashedKey = UUID.randomUUID();
    }

    public UUID getHashedKey() {
        return this.hashedKey;
    }

    /*
    setter not used
    public void setHashedKey(String hashedKey) {
        this.hashedKey = hashedKey;
    }

     */
}
