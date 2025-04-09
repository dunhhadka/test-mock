package com.example.demo.validate;

import com.example.demo.entity.enums.Gender;
import com.example.demo.validate.GenderValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class GenderValidatorTest {

    private GenderValidator genderValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        genderValidator = new GenderValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testIsValid_whenGenderIsInvalid_shouldReturnFalse() {
        assertFalse(genderValidator.isValid("UNKNOWN", context));
        assertFalse(genderValidator.isValid("123", context));
        assertFalse(genderValidator.isValid("malee", context));
    }

    @Test
    void testIsValid_whenGenderIsNull_shouldReturnFalse() {
        assertFalse(genderValidator.isValid(null, context));
    }

    @Test
    void testIsValid_whenGenderIsEmpty_shouldReturnFalse() {
        assertFalse(genderValidator.isValid("", context));
    }
}
