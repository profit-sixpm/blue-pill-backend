package com.sixpm.domain.announcement.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 지역코드
 */
@Getter
@RequiredArgsConstructor
public enum SUBSCRPT_AREA_CODE {
    SEOUL("11", "서울특별시"),
    BUSAN("26", "부산광역시"),
    DAEGU("27", "대구광역시"),
    INCHEON("28", "인천광역시"),
    GWANGJU("29", "광주광역시"),
    DAEJEON("30", "대전광역시"),
    ULSAN("31", "울산광역시"),
    SEJONG("36", "세종특별자치시"),
    GYEONGGI("41", "경기도"),
    GANGWON("42", "강원도"),
    CHUNGBUK("43", "충청북도"),
    CHUNGNAM("44", "충청남도"),
    JEONBUK("45", "전라북도"),
    JEONNAM("46", "전라남도"),
    GYEONGBUK("47", "경상북도"),
    GYEONGNAM("48", "경상남도"),
    JEJU("50", "제주특별자치도");

    private final String code;
    private final String description;

    public static SUBSCRPT_AREA_CODE fromCode(String code) {
        for (SUBSCRPT_AREA_CODE area : values()) {
            if (area.code.equals(code)) {
                return area;
            }
        }
        return null;
    }
}

