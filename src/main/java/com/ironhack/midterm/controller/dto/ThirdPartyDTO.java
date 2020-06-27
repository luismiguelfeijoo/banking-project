package com.ironhack.midterm.controller.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ThirdPartyDTO {

    @NotNull(message = "Name is Mandatory")
    @NotEmpty(message = "Name can't be empty")
    private String name;

    @NotNull(message = "Username is Mandatory")
    @NotEmpty(message = "Username can't be empty")
    private String username;

    public ThirdPartyDTO(@NotNull @NotEmpty String name, @NotNull @NotEmpty String username) {

        this.name = name;
        this.username = username;
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
