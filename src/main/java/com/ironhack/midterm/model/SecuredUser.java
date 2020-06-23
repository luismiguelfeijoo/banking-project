package com.ironhack.midterm.model;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class SecuredUser extends User {
    private String password;

    @OneToMany(fetch= FetchType.EAGER, cascade= CascadeType.ALL, mappedBy="user")
    private Set<Role> roles = new HashSet<>();

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
