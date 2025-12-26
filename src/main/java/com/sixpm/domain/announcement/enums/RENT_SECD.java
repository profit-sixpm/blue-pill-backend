package com.sixpm.domain.announcement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 분양구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum RENT_SECD {
    BUNYANG("0", "분양"),
    IMADAE("1", "임대");

    private final String code;
    private final String description;

    public static RENT_SECD fromCode(String code) {
        for (RENT_SECD type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

