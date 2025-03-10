package com.snackoverflow.toolgether.global.exception.custom.location;

// 주소 변환 관련 예외
public class AddressConversionException extends LocationException{
    public AddressConversionException(String message) {
        super(message);
    }
}
