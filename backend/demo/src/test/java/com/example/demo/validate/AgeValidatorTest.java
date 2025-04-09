package com.example.demo.validate;

import com.example.demo.validate.AgeValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class AgeValidatorTest {

    private AgeValidator ageValidator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        ageValidator = new AgeValidator();
        context = mock(ConstraintValidatorContext.class);
    }

    @Test
    void testIsValid_whenAgeIs18_shouldReturnTrue() {
        String date18YearsAgo = LocalDate.now().minusYears(18).toString(); // yyyy-MM-dd
        assertTrue(ageValidator.isValid(date18YearsAgo, context));
    }

    @Test
    void testIsValid_whenAgeIsUnder18_shouldReturnFalse() {
        String date = LocalDate.now().minusYears(17).plusDays(1).toString();
        assertFalse(ageValidator.isValid(date, context));
    }

    @Test
    void testIsValid_whenDateIsInvalid_shouldReturnFalse() {
        String invalidDate = "not-a-date";
        assertFalse(ageValidator.isValid(invalidDate, context));
    }

    @Test
    void testIsValid_whenDateIsNull_shouldReturnFalse() {
        assertFalse(ageValidator.isValid(null, context));
    }

    @Test
    void testIsValid_whenAgeIsOver18_shouldReturnTrue() {
        String date = LocalDate.now().minusYears(25).toString();
        assertTrue(ageValidator.isValid(date, context));
    }
}
