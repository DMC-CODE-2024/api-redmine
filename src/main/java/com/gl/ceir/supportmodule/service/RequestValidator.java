package com.gl.ceir.supportmodule.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RequestValidator {

    public static LocalDate convertStringDateToLocalDate(String date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
            String formattedDate = parsedDate.format(dateFormatter);
            if (!formattedDate.equals(date)) {
                throw new RuntimeException("Invalid date format. Expected format: yyyy-MM-dd");
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    public static void validateTimes(String startTime, String endTime) {
        LocalDate startDate = convertStringDateToLocalDate(startTime);
        LocalDate endDate = convertStringDateToLocalDate(endTime);
        if (startDate != null || endDate != null) {
            if (startDate == null) {
                throw new RuntimeException("INVALID_START_TIME");
            } else if (endDate == null) {
                throw new RuntimeException("INVALID_END_TIME");
            } else if (endDate.isBefore(startDate)) {
                throw new RuntimeException("END DATE LESS THAN START DATE");
            } else if (endDate.isAfter(LocalDate.now())) {
                throw new RuntimeException("END DATE CAN'T BE IN FUTURE");
            }
        }
    }

    public static void validatePagination(Integer page, Integer size, Integer limit) {
        if (limit == null) {
            limit = 20;
        }
        if (page == null) {
            throw new RuntimeException("INVALID_PAGINATION: page param cannot be null");
        } else if (size == null) {
            throw new RuntimeException("INVALID_PAGINATION: size param cannot be null");
        } else if (page < 0) {
            throw new RuntimeException("INVALID_PAGINATION: Page number should be positive integer");
        } else if (size < 1 || size > 20) {
            throw new RuntimeException("INVALID_PAGINATION: Page Size minimum is 1 maximum is 20");
        }
    }
}
