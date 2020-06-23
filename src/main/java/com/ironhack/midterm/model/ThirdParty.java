package com.ironhack.midterm.model;

import javax.persistence.Entity;

@Entity
public class ThirdParty extends User {
    private String hashedKey;

    public String getHashedKey() {
        return hashedKey;
    }

    public void setHashedKey(String hashedKey) {
        this.hashedKey = hashedKey;
    }
}
