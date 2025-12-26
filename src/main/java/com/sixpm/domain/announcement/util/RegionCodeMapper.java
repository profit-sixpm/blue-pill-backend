package com.sixpm.domain.announcement.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 지역명 -> 지역코드 매핑 유틸리티
 * 행정구역 코드 기준
 */
public class RegionCodeMapper {

    private static final Map<String, String> REGION_CODE_MAP = new HashMap<>();

    static {
        // 특별시/광역시
        REGION_CODE_MAP.put("서울특별시", "11");
        REGION_CODE_MAP.put("서울", "11");
        REGION_CODE_MAP.put("부산광역시", "26");
        REGION_CODE_MAP.put("부산", "26");
        REGION_CODE_MAP.put("대구광역시", "27");
        REGION_CODE_MAP.put("대구", "27");
        REGION_CODE_MAP.put("인천광역시", "28");
        REGION_CODE_MAP.put("인천", "28");
        REGION_CODE_MAP.put("광주광역시", "29");
        REGION_CODE_MAP.put("광주", "29");
        REGION_CODE_MAP.put("대전광역시", "30");
        REGION_CODE_MAP.put("대전", "30");
        REGION_CODE_MAP.put("울산광역시", "31");
        REGION_CODE_MAP.put("울산", "31");
        REGION_CODE_MAP.put("세종특별자치시", "36");
        REGION_CODE_MAP.put("세종", "36");

        // 도
        REGION_CODE_MAP.put("경기도", "41");
        REGION_CODE_MAP.put("경기", "41");
        REGION_CODE_MAP.put("강원특별자치도", "42");
        REGION_CODE_MAP.put("강원도", "42");
        REGION_CODE_MAP.put("강원", "42");
        REGION_CODE_MAP.put("충청북도", "43");
        REGION_CODE_MAP.put("충북", "43");
        REGION_CODE_MAP.put("충청남도", "44");
        REGION_CODE_MAP.put("충남", "44");
        REGION_CODE_MAP.put("전라북도", "45");
        REGION_CODE_MAP.put("전북", "45");
        REGION_CODE_MAP.put("전북특별자치도", "45");
        REGION_CODE_MAP.put("전라남도", "46");
        REGION_CODE_MAP.put("전남", "46");
        REGION_CODE_MAP.put("경상북도", "47");
        REGION_CODE_MAP.put("경북", "47");
        REGION_CODE_MAP.put("경상남도", "48");
        REGION_CODE_MAP.put("경남", "48");
        REGION_CODE_MAP.put("제주특별자치도", "50");
        REGION_CODE_MAP.put("제주도", "50");
        REGION_CODE_MAP.put("제주", "50");
    }

    /**
     * 지역명으로 지역코드 조회
     *
     * @param regionName 지역명 (예: "서울특별시", "경기도", "광주광역시")
     * @return 지역코드 (예: "11", "41", "29") 또는 null (매핑되지 않은 경우)
     */
    public static String getRegionCode(String regionName) {
        if (regionName == null || regionName.trim().isEmpty()) {
            return null;
        }

        // 정확히 일치하는 경우
        String code = REGION_CODE_MAP.get(regionName.trim());
        if (code != null) {
            return code;
        }

        // 포함 관계로 찾기 (예: "서울특별시 강남구" -> "11")
        for (Map.Entry<String, String> entry : REGION_CODE_MAP.entrySet()) {
            if (regionName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * 지역코드가 유효한지 확인
     *
     * @param regionCode 지역코드
     * @return 유효 여부
     */
    public static boolean isValidRegionCode(String regionCode) {
        return regionCode != null && REGION_CODE_MAP.containsValue(regionCode);
    }
}

