package com.ironhack.midterm.model;

import javax.persistence.Entity;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Entity
public class ThirdParty extends User {
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

    /*
    setter not used
    public void setHashedKey(String hashedKey) {
        this.hashedKey = hashedKey;
    }

     */
}
