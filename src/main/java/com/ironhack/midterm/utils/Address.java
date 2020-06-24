package com.ironhack.midterm.utils;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Embeddable
public class Address {
    @NotNull
    @NotBlank
    private String street;
    @NotNull
    @NotBlank
    private String city;
    @NotNull
    @NotBlank
    private String country;
    @NotNull
    @NotBlank
    private String zip;

    public Address(String street, String city, String country, String zip) {
        this.street = street;
        this.city = city;
        this.country = country;
        this.zip = zip;
    }

    public Address() {
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
