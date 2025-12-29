package com.CinemaManager.Cinema.booking.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void home_ShouldReturnServiceInfo() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("Cinema Booking API"))
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.endpoints").isArray());
    }

    @Test
    void testEndpoint_ShouldReturnApiWorking() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/test"))
                .andExpect(status().isOk())
                .andExpect(content().string("API is working! ✅"));
    }
}