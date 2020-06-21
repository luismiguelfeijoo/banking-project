package com.ironhack.midterm.model;

import com.ironhack.midterm.utils.Address;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class AccountHolder extends User {

    private Date dateOfBirth;
    @Embedded
    private Address primaryAddress;
    @Embedded
    private Address mailingAddress;
    @OneToMany(mappedBy = "primaryOwner")
    private List<Account> primaryAccounts;
    @OneToMany(mappedBy = "secondaryOwner")
    private List<Account> secondaryAccounts;

    public AccountHolder(String username, String name, Role role, String password, Date dateOfBirth, Address primaryAddress, Address mailingAddress, List<Account> primaryAccounts, List<Account> secondaryAccounts) {

        this.dateOfBirth = dateOfBirth;
        this.primaryAddress = primaryAddress;
        this.mailingAddress = mailingAddress;
        this.primaryAccounts = primaryAccounts;
        this.secondaryAccounts = secondaryAccounts;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Address getPrimaryAddress() {
        return primaryAddress;
    }

    public void setPrimaryAddress(Address primaryAddress) {
        this.primaryAddress = primaryAddress;
    }

    public Address getMailingAddress() {
        return mailingAddress;
    }

    public void setMailingAddress(Address mailingAddress) {
        this.mailingAddress = mailingAddress;
    }

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


}
