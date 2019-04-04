package com.github.danielfernandez.bytecodetest;

public class User {

    private final String name;
    private final String surname;

    public User(final String name, final String surname) {
        super();
        this.name = name;
        this.surname = surname;
    }

    public String getName() {
        return this.name;
    }


    public String getSurname() {
        return this.surname;
    }
}
