package com.example.demo.utils;

import com.example.demo.exception.BaseException;
import com.example.demo.utils.DateFormat;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DateFormatTest {

    @Test
    @DisplayName("stringToDate - convert valid string to SQL Date")
    public void stringToDate_validInput_success() {
        String input = "2024-04-09";
        Date expectedDate = Date.valueOf(LocalDate.of(2024, 4, 9));
        Date result = DateFormat.stringToDate(input);

        Assertions.assertEquals(expectedDate, result);
    }

    @Test
    @DisplayName("stringToDate - throw exception for invalid format")
    public void stringToDate_invalidInput_throwsException() {
        String input = "09-04-2024"; // sai format
        Assertions.assertThrows(Exception.class, () -> {
            DateFormat.stringToDate(input);
        });
    }

    @Test
    @DisplayName("toLocalDateTimeInParam - valid input returns start of day")
    public void toLocalDateTimeInParam_valid_success() {
        String input = "2024-04-09";
        LocalDateTime expected = LocalDate.of(2024, 4, 9).atStartOfDay();
        LocalDateTime result = DateFormat.toLocalDateTimeInParam(input);

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("toLocalDateTimeInParam - invalid input throws BaseException")
    public void toLocalDateTimeInParam_invalid_throwsBaseException() {
        String input = "invalid-date";
        Assertions.assertThrows(BaseException.class, () -> {
            DateFormat.toLocalDateTimeInParam(input);
        });
    }

    @Test
    @DisplayName("toLocalDateTime - check = false returns start of day")
    public void toLocalDateTime_checkFalse_success() {
        String input = "2024-04-09";
        LocalDateTime expected = LocalDate.of(2024, 4, 9).atStartOfDay();
        LocalDateTime result = DateFormat.toLocalDateTime(input, false);

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("toLocalDateTime - check = true returns next day start of day")
    public void toLocalDateTime_checkTrue_success() {
        String input = "2024-04-09";
        LocalDateTime expected = LocalDate.of(2024, 4, 10).atStartOfDay();
        LocalDateTime result = DateFormat.toLocalDateTime(input, true);

        Assertions.assertEquals(expected, result);
    }

    @Test
    @DisplayName("toLocalDateTime - invalid input throws BaseException")
    public void toLocalDateTime_invalidInput_throwsBaseException() {
        String input = "09-04-2024";
        Assertions.assertThrows(BaseException.class, () -> {
            DateFormat.toLocalDateTime(input, false);
        });
    }
}
