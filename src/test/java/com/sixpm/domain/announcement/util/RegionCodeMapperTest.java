package com.sixpm.domain.announcement.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RegionCodeMapper 테스트
 */
class RegionCodeMapperTest {

    @Test
    void testGetRegionCode_정확한매칭() {
        // 특별시/광역시
        assertThat(RegionCodeMapper.getRegionCode("서울특별시")).isEqualTo("11");
        assertThat(RegionCodeMapper.getRegionCode("부산광역시")).isEqualTo("26");
        assertThat(RegionCodeMapper.getRegionCode("인천광역시")).isEqualTo("28");
        assertThat(RegionCodeMapper.getRegionCode("광주광역시")).isEqualTo("29");
        assertThat(RegionCodeMapper.getRegionCode("대전광역시")).isEqualTo("30");
        assertThat(RegionCodeMapper.getRegionCode("세종특별자치시")).isEqualTo("36");

        // 도
        assertThat(RegionCodeMapper.getRegionCode("경기도")).isEqualTo("41");
        assertThat(RegionCodeMapper.getRegionCode("강원도")).isEqualTo("42");
        assertThat(RegionCodeMapper.getRegionCode("충청북도")).isEqualTo("43");
        assertThat(RegionCodeMapper.getRegionCode("제주특별자치도")).isEqualTo("50");
    }

    @Test
    void testGetRegionCode_약칭() {
        assertThat(RegionCodeMapper.getRegionCode("서울")).isEqualTo("11");
        assertThat(RegionCodeMapper.getRegionCode("경기")).isEqualTo("41");
        assertThat(RegionCodeMapper.getRegionCode("광주")).isEqualTo("29");
        assertThat(RegionCodeMapper.getRegionCode("제주")).isEqualTo("50");
    }

    @Test
    void testGetRegionCode_포함관계() {
        // "서울특별시 강남구" -> "11"
        assertThat(RegionCodeMapper.getRegionCode("서울특별시 강남구")).isEqualTo("11");
        assertThat(RegionCodeMapper.getRegionCode("경기도 수원시")).isEqualTo("41");
        assertThat(RegionCodeMapper.getRegionCode("부산광역시 해운대구")).isEqualTo("26");
    }

    @Test
    void testGetRegionCode_null또는빈값() {
        assertThat(RegionCodeMapper.getRegionCode(null)).isNull();
        assertThat(RegionCodeMapper.getRegionCode("")).isNull();
        assertThat(RegionCodeMapper.getRegionCode("   ")).isNull();
    }

    @Test
    void testGetRegionCode_매핑되지않는값() {
        assertThat(RegionCodeMapper.getRegionCode("알수없음")).isNull();
        assertThat(RegionCodeMapper.getRegionCode("외국")).isNull();
    }

    @Test
    void testIsValidRegionCode() {
        assertThat(RegionCodeMapper.isValidRegionCode("11")).isTrue();
        assertThat(RegionCodeMapper.isValidRegionCode("41")).isTrue();
        assertThat(RegionCodeMapper.isValidRegionCode("29")).isTrue();
        assertThat(RegionCodeMapper.isValidRegionCode("50")).isTrue();

        assertThat(RegionCodeMapper.isValidRegionCode("99")).isFalse();
        assertThat(RegionCodeMapper.isValidRegionCode(null)).isFalse();
        assertThat(RegionCodeMapper.isValidRegionCode("")).isFalse();
    }
}

