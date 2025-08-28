package com.artists_heaven.exception;

public class AppExceptions {

    public static class ForbiddenActionException extends RuntimeException {
        public ForbiddenActionException(String message) {
            super(message);
        }
    }

    public static class DuplicateActionException extends RuntimeException {
        public DuplicateActionException(String message) {
            super(message);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String message) {
            super(message);
        }

        public EmailSendException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class BadRequestException extends RuntimeException {
        public BadRequestException(String message) {
            super(message);
        }
    }

    public static class UnauthorizedActionException extends RuntimeException {
        public UnauthorizedActionException(String message) {
            super(message);
        }
    }

    public static class InvalidInputException extends RuntimeException {
        public InvalidInputException(String message) {
            super(message);
        }
    }

    public static class LimitExceededException extends RuntimeException {
        public LimitExceededException(String message) {
            super(message);
        }
    }
}
