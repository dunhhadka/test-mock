package com.example.demo.validate;

import com.example.demo.entity.enums.Role;
import com.example.demo.validate.AuthorValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AuthorValidatorTest {

    private AuthorValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new AuthorValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testIsValid_withValidRoles_shouldReturnTrue() {
        for (Role role : Role.values()) {
            assertTrue(validator.isValid(role.name(), context));
            assertTrue(validator.isValid(role.name().toLowerCase(), context)); // Case-insensitive check
        }
    }

    @Test
    void testIsValid_withInvalidRoles_shouldReturnFalse() {
        assertFalse(validator.isValid("NOT_A_ROLE", context));
        assertFalse(validator.isValid("Adminn", context));
        assertFalse(validator.isValid("123", context));
    }

    @Test
    void testIsValid_withNull_shouldReturnFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testIsValid_withEmptyString_shouldReturnFalse() {
        assertFalse(validator.isValid("", context));
    }
}
