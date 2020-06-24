package com.ironhack.midterm.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
public class User {

    @NotNull
    @NotEmpty
    private String username;
    @NotNull
    @NotEmpty
    private String name;

    public User() {
    }

    public User(@NotNull @NotEmpty String username, @NotNull @NotEmpty String name) {
        this.username = username;
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}

