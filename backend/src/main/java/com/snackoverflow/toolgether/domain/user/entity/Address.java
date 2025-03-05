package com.snackoverflow.toolgether.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(nullable = false)
    private String mainAddress; // 00시 00구 00동

    @Column(nullable = false)
    private String detailAddress; // 00아파트 00동 00호

    private String zipcode; // 우편 번호
}
