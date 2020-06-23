package com.ironhack.midterm.controller.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ThirdPartyDTO {
    @NotNull
    @NotEmpty
    private String key;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    @NotEmpty
    private String username;

    public ThirdPartyDTO(@NotNull @NotEmpty String key, @NotNull @NotEmpty String name, @NotNull @NotEmpty String username) {
        this.key = key;
        this.name = name;
        this.username = username;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
