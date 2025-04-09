package com.example.demo.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HandleAccessDeniedTest {

    private HandleAccessDenied handleAccessDenied;

    @BeforeEach
    void setUp() {
        handleAccessDenied = new HandleAccessDenied();
    }

    @Test
    void testHandleAccessDenied() throws IOException, ServletException {
        // Set up mock request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected-resource");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // Set up mock authentication with role USER
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("testuser", null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")))
        );

        // Access denied exception
        AccessDeniedException exception = new AccessDeniedException("Access is denied");

        // Call method
        handleAccessDenied.handle(request, response, exception);

        // Assert response
        assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
        assertEquals("UTF-8", response.getCharacterEncoding());

        // Parse and verify JSON body
        ObjectMapper mapper = new ObjectMapper();
        String responseBody = response.getContentAsString();

        assertTrue(responseBody.contains("403")); // Status
        assertTrue(responseBody.contains("Bạn có Role là:")); // Message
        assertTrue(responseBody.contains("/api/protected-resource")); // URL
    }
}
