package com.finguard.core.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finguard.core.dto.RegisterRequestDTO;
import com.finguard.core.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class AuthControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper; 

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        this.objectMapper = new ObjectMapper();

       this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void deberiaRegistrarUsuarioViaHttpYDevolver201() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setEmail("controlador@test.com");
        request.setPassword("passwordHttp123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("controlador@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}