package com.sixpm.domain.announcement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주택구분 코드
 */
@Getter
@RequiredArgsConstructor
public enum APT_TYPE {
    APT("01", "아파트"),
    DASEDAE("02", "다세대"),
    YUNLIP("03", "연립"),
    MINJAYOUNG("04", "민영주택"),
    GUKMIN("05", "국민주택");

    private final String code;
    private final String description;

    public static APT_TYPE fromCode(String code) {
        for (APT_TYPE type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}

