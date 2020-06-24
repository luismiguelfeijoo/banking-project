package com.ironhack.midterm.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class ThirdParty extends User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotNull
    private String hashedKey;

    public ThirdParty() {
    }

    public ThirdParty(@NotNull @NotEmpty String username, @NotNull @NotEmpty String name, @NotNull String hashedKey) {
        super(username, name);
        this.hashedKey = hashedKey;
    }

    public String getHashedKey() {
        return hashedKey;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    /*
    setter not used
    public void setHashedKey(String hashedKey) {
        this.hashedKey = hashedKey;
    }

     */
}
