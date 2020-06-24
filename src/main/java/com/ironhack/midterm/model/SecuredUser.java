package com.ironhack.midterm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class SecuredUser extends User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @NotEmpty
    private String password;

    @ManyToMany(fetch= FetchType.EAGER, cascade= CascadeType.ALL, mappedBy="user")
    @JsonIgnore
    private Set<Role> roles = new HashSet<>();

    public SecuredUser() {
    }

    public SecuredUser(@NotNull @NotEmpty String username, @NotNull @NotEmpty String name, @NotNull @NotEmpty String password) {
        super(username, name);
        this.password = password;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Set<Role> getRoles() {
        return roles;
    }
    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
