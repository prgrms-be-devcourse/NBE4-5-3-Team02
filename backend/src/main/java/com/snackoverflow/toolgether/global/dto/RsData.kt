package com.snackoverflow.toolgether.global.dto

/**
 * TODO 변환 이후 @JvmOverloads constructor 제거할 것
 */

data class RsData<T> @JvmOverloads constructor (
    val resultCode: String,
    val msg: String,
    val data: T? = null // 기본값을 null로 설정
)