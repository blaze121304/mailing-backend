package com.rusty.mailingbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rusty.mailingbackend.dto.SubscribeRequest;
import com.rusty.mailingbackend.repository.SubscriptionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SubscribeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    SubscriptionRepository subscriptionRepository;

    @Test
    void subscribe_success_returns200() throws Exception {
        subscriptionRepository.deleteAll();
        SubscribeRequest request = new SubscribeRequest("user@example.com", List.of("IT", "ECONOMY"));

        mockMvc.perform(post("/api/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void subscribe_duplicateEmail_returns400() throws Exception {
        subscriptionRepository.deleteAll();
        SubscribeRequest request = new SubscribeRequest("dup@example.com", List.of("IT"));
        mockMvc.perform(post("/api/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/subscribe")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
