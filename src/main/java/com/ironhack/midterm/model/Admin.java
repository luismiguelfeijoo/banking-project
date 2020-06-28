package com.ironhack.midterm.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;


@Entity
public class Admin extends SecuredUser {
}

