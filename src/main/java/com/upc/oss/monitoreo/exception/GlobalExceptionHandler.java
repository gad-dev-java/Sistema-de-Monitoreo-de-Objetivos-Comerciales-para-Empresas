package com.upc.oss.monitoreo.exception;

import com.upc.oss.monitoreo.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex,
                                                                            HttpServletRequest request) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> {
                    String[] pathParts = violation.getPropertyPath().toString().split("\\.");
                    return pathParts[pathParts.length - 1] + ": " + violation.getMessage();
                })
                .sorted()
                .toList();
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Validation incorrect", errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .toList();
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, "Validation incorrect", errors);
    }

    @ExceptionHandler(CompanyAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleCompanyAlreadyExistsException(
            CompanyAlreadyExistsException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(CompanyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCompanyNotFoundException(
            CompanyNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFoundException(
            NotificationNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ObjectiveStoreActiveNotFound.class)
    public ResponseEntity<ErrorResponse> handleObjectiveStoreActiveNotFound(
            ObjectiveStoreActiveNotFound ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(RoleInvalidException.class)
    public ResponseEntity<ErrorResponse> handleRoleInvalidException(
            RoleInvalidException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(StoreNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStoreNotFoundException(
            StoreNotFoundException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentialsException(
            BadCredentialsException ex, HttpServletRequest request) {
        return buildErrorResponse(request, HttpStatus.UNAUTHORIZED, "Invalid email or password", null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpServletRequest request,
                                                             HttpStatus status,
                                                             String message,
                                                             List<String> errors) {
        String fullPath = request.getRequestURI();
        String query = request.getQueryString();
        String pathWithParams = query != null ? fullPath + "?" + query : fullPath;
        String methodHttp = request.getMethod();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(status.value())
                .message(message)
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .path(methodHttp + ": " + pathWithParams)
                .build();

        return ResponseEntity.status(status).body(errorResponse);
    }
}
