package com.fluxbanker.api.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthIntegrationTest {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void testFullAuthFlow() throws Exception {
                // 1. Register
                String registerJson = """
                                {
                                    "email": "newuser@example.com",
                                    "password": "Password123!",
                                    "firstName": "New",
                                    "lastName": "User",
                                    "dateOfBirth": "1990-01-01",
                                    "aadhaar": "123456789012",
                                    "address1": "123 Main St",
                                    "city": "Mumbai",
                                    "state": "MH",
                                    "pinCode": "400001"
                                }
                                """;

                mockMvc.perform(post("/api/v1/auth/register")
                                .content(registerJson)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.user.email").value("newuser@example.com"))
                                .andExpect(jsonPath("$.user.aadhaar").value("123456789012"));

                // 2. Login
                String loginJson = """
                                {
                                    "email": "newuser@example.com",
                                    "password": "Password123!"
                                }
                                """;

                var loginResult = mockMvc.perform(post("/api/v1/auth/login")
                                .content(loginJson)
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists())
                                .andReturn();

                String accessToken = com.jayway.jsonpath.JsonPath.read(loginResult.getResponse().getContentAsString(),
                                "$.accessToken");
                jakarta.servlet.http.Cookie refreshCookie = loginResult.getResponse().getCookie("refresh_token");

                assert refreshCookie != null;

                // 3. Fetch User Profile
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/v1/users/me")
                                .header("Authorization", "Bearer " + accessToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("newuser@example.com"));

                // 4. Refresh Token
                mockMvc.perform(post("/api/v1/auth/refresh")
                                .cookie(refreshCookie))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").exists());
        }
}
