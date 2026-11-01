package com.mahmoud.coursehub;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Calls the real controllers through MockMvc (no network). Each test is rolled back.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryApiTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createCategory_returns201() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Programming\",\"description\":\"Code\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Programming"));
    }

    @Test
    void createCategory_blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.name").exists());
    }

    @Test
    void createCategory_duplicateName_returns409() throws Exception {
        String body = "{\"name\":\"Design\"}";
        mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/categories").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnknownCategory_returns404() throws Exception {
        mockMvc.perform(get("/api/categories/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Category with id 999999 not found"));
    }
}
