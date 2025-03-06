package com.snackoverflow.toolgether.global.exception.custom.user;

public class UserNotFoundException extends RuntimeException{

    public UserNotFoundException(String message) {
        super(message);
    }
}
