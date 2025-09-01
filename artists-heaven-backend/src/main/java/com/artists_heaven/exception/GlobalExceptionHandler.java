package com.artists_heaven.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.artists_heaven.standardResponse.StandardResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppExceptions.ResourceNotFoundException.class)
    public ResponseEntity<StandardResponse<String>> handleResourceNotFound(AppExceptions.ResourceNotFoundException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AppExceptions.ForbiddenActionException.class)
    public ResponseEntity<StandardResponse<String>> handleForbidden(AppExceptions.ForbiddenActionException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AppExceptions.DuplicateActionException.class)
    public ResponseEntity<StandardResponse<String>> handleDuplicateRating(AppExceptions.DuplicateActionException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppExceptions.EmailSendException.class)
    public ResponseEntity<StandardResponse<String>> handleEmailError(AppExceptions.EmailSendException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(AppExceptions.UnauthorizedActionException.class)
    public ResponseEntity<StandardResponse<String>> handleAuthoriseError(AppExceptions.UnauthorizedActionException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AppExceptions.InvalidInputException.class)
    public ResponseEntity<StandardResponse<String>> handleInvalidInput(AppExceptions.InvalidInputException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppExceptions.LimitExceededException.class)
    public ResponseEntity<StandardResponse<String>> hannldeLimitExceededException(AppExceptions.LimitExceededException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.TOO_MANY_REQUESTS);
    }

    @ExceptionHandler(AppExceptions.BadRequestException.class)
    public ResponseEntity<StandardResponse<String>> hannldeBadRequest(AppExceptions.BadRequestException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

     @ExceptionHandler(AppExceptions.InternalServerErrorException.class)
    public ResponseEntity<StandardResponse<String>> handleInternalServerError(AppExceptions.InternalServerErrorException ex) {
        return buildErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }



    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<StandardResponse<String>> handleValidationErrors(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("Invalid input");

        return buildErrorResponse(errorMessage, HttpStatus.BAD_REQUEST);
    }

    // Captura cualquier excepción no controlada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardResponse<String>> handleGenericException(Exception ex) {
        return buildErrorResponse("Unexpected error occurred" + ex, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Método de utilidad para construir la respuesta estándar
    private ResponseEntity<StandardResponse<String>> buildErrorResponse(String message, HttpStatus status) {
        return ResponseEntity
                .status(status)
                .body(new StandardResponse<>(message, status.value()));
    }
}
