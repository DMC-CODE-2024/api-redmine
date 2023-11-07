package com.gl.ceir.supportmodule.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RequestValidator {


    public static void validate(String startTime, String endTime, Integer page, Integer size, Integer limit) {
        LocalDate startDate = convertStringDateToLocalDate(startTime);
        LocalDate endDate = convertStringDateToLocalDate(endTime);

        validateTimes(startDate, endDate);
        validatePagination(page, size, limit);
    }

    private static LocalDate convertStringDateToLocalDate(String date) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            LocalDate parsedDate = LocalDate.parse(date, dateFormatter);
            String formattedDate = parsedDate.format(dateFormatter);
            if (!formattedDate.equals(date)) {
                throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
            }
            return parsedDate;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid date format. Expected format: yyyy-MM-dd");
        }
    }

    private static void validateTimes(LocalDate startTime, LocalDate endTime) {
        if (startTime != null || endTime != null) {
            if (startTime == null) {
                throw new IllegalArgumentException("INVALID_START_TIME");
            } else if (endTime == null) {
                throw new IllegalArgumentException("INVALID_END_TIME");
            } else if (endTime.isBefore(startTime)) {
                throw new IllegalArgumentException("INVALID_TIME_RANGE");
            } else if (endTime.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("INVALID_END_DATE");
            }
        }
    }

    public static void validatePagination(Integer page, Integer size, Integer limit) {
        if (limit == null) {
            limit = 20;
        }
        if (page == null) {
            throw new IllegalArgumentException("INVALID_PAGINATION: page param cannot be null");
        } else if (size == null) {
            throw new IllegalArgumentException("INVALID_PAGINATION: size param cannot be null");
        } else if (page < 0) {
            throw new IllegalArgumentException("INVALID_PAGINATION: Page number should be positive integer");
        } else if (size < 1 || size > 20) {
            throw new IllegalArgumentException("INVALID_PAGINATION: Page Size minimum is 1 maximum is 20");
        }
    }
}
