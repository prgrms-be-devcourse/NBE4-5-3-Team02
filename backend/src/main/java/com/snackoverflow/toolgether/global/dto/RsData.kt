package com.snackoverflow.toolgether.global.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.NonNull;

@AllArgsConstructor
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RsData<T> {

    @NonNull
    private String code;
    @NonNull
    private String msg;

    private T data;

    public RsData(String code, String msg) {
        this(code, msg, null);
    }

    @JsonIgnore
    public int getStatusCode() {
        String statusCodeStr = code.split("-")[0];
        return Integer.parseInt(statusCodeStr);
    }
        // 성공 여부 확인하는 메서드
        public boolean isSuccess () {
            return code.startsWith("2"); // 2xx 응답이면 성공
        }

}
