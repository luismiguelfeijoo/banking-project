package com.ironhack.midterm.security;

import com.ironhack.midterm.model.Account;
import com.ironhack.midterm.model.Role;
import com.ironhack.midterm.model.SecuredUser;
import com.ironhack.midterm.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CustomSecurityUser extends SecuredUser implements UserDetails {
    private static final long serialVersionUID = -7373627892222L;

    private final static Logger LOGGER = LogManager.getLogger(CustomSecurityUser.class);

    public CustomSecurityUser(SecuredUser user) {
        LOGGER.info("[AUTHORIZATION INIT] UserId:" + user.getId());
        this.setRoles(user.getRoles());
        this.setId(user.getId());
        this.setPassword(user.getPassword());
        this.setUsername(user.getUsername());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new HashSet<>();
        Set<Role> roles = this.getRoles();
        //System.out.println(this);
        for( Role role : roles ) {

            LOGGER.info("[GETTING AUTHORITIES] Role:" + role.getRole());
            //System.out.println(role.getRole());
            authorities.add( new SimpleGrantedAuthority(role.getRole()) );
        }
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
