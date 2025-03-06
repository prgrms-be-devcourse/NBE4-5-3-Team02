package com.snackoverflow.toolgether.domain.user.dto;

import com.snackoverflow.toolgether.domain.user.entity.Address;

public record AddressInfo(
        String mainAddress,
        String detailAddress,
        String zipcode
) {
    public static AddressInfo from(Address address) {
        return new AddressInfo(
                address.getMainAddress(),
                address.getDetailAddress(),
                address.getZipcode()
        );
    }
}
