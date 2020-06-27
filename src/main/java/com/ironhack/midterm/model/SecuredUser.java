package com.ironhack.midterm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ironhack.midterm.utils.Hashing;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class SecuredUser extends User {
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
        this.password = Hashing.hash(password);
    }

    public String getPassword() {
        return password;
    }

    public Set<Role> getRoles() {
        return roles;
    }


    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
