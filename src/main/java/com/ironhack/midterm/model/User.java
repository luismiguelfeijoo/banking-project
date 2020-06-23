package com.ironhack.midterm.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@MappedSuperclass
public class User {

    @Id
    @GeneratedValue(strategy= GenerationType.TABLE)
    private Long id;
    private String username;
    private String name;



    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
}

