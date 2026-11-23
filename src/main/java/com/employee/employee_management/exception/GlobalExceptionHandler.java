package com.employee.employee_management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeNotFound(EmployeeNotFoundException ex) {
        Map<String, Object> response = Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "errors", errors
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmail(DuplicateEmailException ex) {
        Map<String, Object> response = Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        Map<String, Object> response = Map.of(
                "message", "Email already registered",
                "status", HttpStatus.CONFLICT.value()
        );

        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(
            IllegalArgumentException ex) {

        Map<String, Object> response = Map.of(
                "message", ex.getMessage(),
                "status", HttpStatus.BAD_REQUEST.value()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex) {

        String parameterName = ex.getName();
        Class<?> requiredType = ex.getRequiredType();

        String expectedType = requiredType != null
                ? requiredType.getSimpleName()
                : "required data type";

        String message = parameterName
                + " must be a valid value of type "
                + expectedType;

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", message
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            NoResourceFoundException ex) {

        Map<String, Object> response = Map.of(
                "status", HttpStatus.NOT_FOUND.value(),
                "message", "Requested API endpoint not found"
        );

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex) {

        Map<String, Object> response = Map.of(
                "status", HttpStatus.METHOD_NOT_ALLOWED.value(),
                "message", ex.getMessage()
        );

        return new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingRequestParam(
            MissingServletRequestParameterException ex) {

        String message = ex.getParameterName() + " parameter is required";

        Map<String, Object> response = Map.of(
                "status", HttpStatus.BAD_REQUEST.value(),
                "message", message
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

}
