package com.example.demo.handle;

import com.example.demo.model.response.RestError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class HandleAuthenticationEntryPointTest {

    private HandleAuthenticationEntryPoint entryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        entryPoint = new HandleAuthenticationEntryPoint();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCommence_withTokenError() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("tokenError", "Invalid token");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationCredentialsNotFoundException("Invalid token"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        RestError error = objectMapper.readValue(response.getContentAsString(), RestError.class);
        assertEquals("401 UNAUTHORIZED", error.getStatusCode());
        assertEquals("Invalid token", error.getMessage());
    }

    @Test
    void testCommence_withUserNotFoundByEmail() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("userNotFoundByEmail", "Email not found");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationCredentialsNotFoundException("Email not found"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        RestError error = objectMapper.readValue(response.getContentAsString(), RestError.class);
        assertEquals("401 UNAUTHORIZED", error.getStatusCode());
        assertEquals("Email not found", error.getMessage());
    }

    @Test
    void testCommence_withTokenIsNull() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute("TokenIsNull", "true");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationCredentialsNotFoundException("Token is null"));

        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        RestError error = objectMapper.readValue(response.getContentAsString(), RestError.class);
        assertEquals("401 UNAUTHORIZED", error.getStatusCode());
    }

    @Test
    void testCommence_withErrorPath() throws IOException, ServletException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/error");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new AuthenticationCredentialsNotFoundException("Path error"));

        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        RestError error = objectMapper.readValue(response.getContentAsString(), RestError.class);
        assertEquals("404 NOT_FOUND", error.getStatusCode());
    }
}
