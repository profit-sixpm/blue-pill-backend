package com.sixpm.presentation.announcement.controller;

import com.sixpm.domain.announcement.dto.request.AnnouncementFetchRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 청약 공고 Admin API 통합 테스트
 */
@SpringBootTest
@AutoConfigureMockMvc
class AnnouncementAdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testFetchAnnouncementsValidationError() throws Exception {
        // Given: 잘못된 날짜 형식
        String invalidRequest = """
                {
                    "announcementDate": "2023-12-25"
                }
                """;

        // When & Then: 400 Bad Request 응답
        mockMvc.perform(post("/api/admin/announcements/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFetchAnnouncementsValidRequest() throws Exception {
        // Given: 유효한 날짜 형식
        String validRequest = """
                {
                    "announcementDate": "20231225"
                }
                """;

        // When & Then: API 호출 성공 (실제 데이터가 없을 수 있으므로 200 또는 실패 응답 확인)
        mockMvc.perform(post("/api/admin/announcements/fetch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.processedCount").exists())
                .andExpect(jsonPath("$.uploadedCount").exists())
                .andExpect(jsonPath("$.failedCount").exists());
    }
}

