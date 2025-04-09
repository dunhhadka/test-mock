package com.example.demo.jwt;

import com.example.demo.entity.AccountEntity;
import com.example.demo.jwt.JwtService;
import com.example.demo.security.CustomUserDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
    }

    @Test
    void testExtractTokenFromRequest() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn("Bearer sometoken123");

        String token = jwtService.extractTokenFromRequest(request);
        assertEquals("sometoken123", token);
    }

    @Test
    void testExtractTokenFromRequest_Null() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("Authorization")).thenReturn(null);

        String token = jwtService.extractTokenFromRequest(request);
        assertNull(token);
    }
}
