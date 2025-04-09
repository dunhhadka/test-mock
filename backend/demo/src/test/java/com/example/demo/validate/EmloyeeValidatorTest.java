package com.example.demo.validate;

import com.example.demo.entity.enums.TypeEmployee;
import com.example.demo.validate.EmloyeeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class EmloyeeValidatorTest {

    private EmloyeeValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new EmloyeeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testIsValid_whenTypeIsValid_shouldReturnTrue() {
        for (TypeEmployee type : TypeEmployee.values()) {
            assertTrue(validator.isValid(type.name(), context));
            assertTrue(validator.isValid(type.name().toLowerCase(), context));
        }
    }

    @Test
    void testIsValid_whenTypeIsInvalid_shouldReturnFalse() {
        assertFalse(validator.isValid("UNKNOWN", context));
        assertFalse(validator.isValid("123", context));
        assertFalse(validator.isValid("Adminn", context));
    }

    @Test
    void testIsValid_whenTypeIsNull_shouldReturnFalse() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testIsValid_whenTypeIsEmpty_shouldReturnFalse() {
        assertFalse(validator.isValid("", context));
    }
}
