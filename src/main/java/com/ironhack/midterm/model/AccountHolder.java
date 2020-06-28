package com.ironhack.midterm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ironhack.midterm.utils.Address;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
public class AccountHolder extends SecuredUser {

    @NotNull
    private Date dateOfBirth;
    @Embedded
    private Address primaryAddress;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name="street", column=@Column(name="mailing_street")),
            @AttributeOverride(name="city", column=@Column(name="mailing_city")),
            @AttributeOverride(name="country", column=@Column(name="mailing_country")),
            @AttributeOverride(name="zip", column=@Column(name="mailing_zip"))
    })
    private Address mailingAddress;

    private boolean loggedIn;
    @OneToMany(mappedBy = "primaryOwner")
    @JsonIgnore
    private List<Account> primaryAccounts;
    @OneToMany(mappedBy = "secondaryOwner")
    @JsonIgnore
    private List<Account> secondaryAccounts;

    // private boolean canAccess;

    /*
    public boolean canAccess(Account account) {
        if (Account.primaryOwner.equals(this)) return true;
        return false;
     }
     */

    public AccountHolder() {
    }

    public AccountHolder(@NotNull @NotEmpty String username, @NotNull @NotEmpty String name, @NotNull @NotEmpty String password, @NotNull Date dateOfBirth, Address primaryAddress) {
        super(username, name, password);
        this.dateOfBirth = dateOfBirth;
        this.primaryAddress = primaryAddress;
        setLoggedIn(false);
    }


    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    /*
    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }
     */

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    /*
    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
    }
     */

    public Address getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(Address mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

    /*
    public List<Account> getPrimaryAccounts() {
        return primaryAccounts;
    }

    public void setPrimaryAccounts(List<Account> primaryAccounts) {
        this.primaryAccounts = primaryAccounts;
    }

    public List<Account> getSecondaryAccounts() {
        return secondaryAccounts;
    }

    public void setSecondaryAccounts(List<Account> secondaryAccounts) {
        this.secondaryAccounts = secondaryAccounts;
    }
     */

    public boolean isLoggedIn() {
        return loggedIn;
    }

    private void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public void login() {
        if (!isLoggedIn()) {
            setLoggedIn(true);
        }
    }

    public void logout() {
        if (isLoggedIn()) {
           setLoggedIn(false);
        }
    }
}
