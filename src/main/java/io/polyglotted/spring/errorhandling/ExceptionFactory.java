package io.polyglotted.spring.errorhandling;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.unbescape.html.HtmlEscape.escapeHtml4;

@SuppressWarnings({"unused", "WeakerAccess", "Serial"})
public abstract class ExceptionFactory {

    public static <T> T checkNotFound(T t, String message) { if (t == null) { throw new NotFoundException(message); } return t; }

    public static NotFoundException notFoundException(String message) { return new NotFoundException(message); }

    public static WebException asInternalServerException(Throwable ex) {
        return ex.getCause() instanceof WebException ? (WebException) ex.getCause() : new WebException(INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }

    public static WebException internalServerException(String message, Throwable ex) { return new WebException(INTERNAL_SERVER_ERROR, message, ex); }

    public static WebException internalServerException(String message) { return new WebException(INTERNAL_SERVER_ERROR, message); }

    public static WebException badRequestException(String message) { return new WebException(BAD_REQUEST, message); }

    public static WebException forbiddenException(String message) { return new WebException(FORBIDDEN, message); }

    public static WebException unauthorisedException(String message) { return new WebException(UNAUTHORIZED, message); }

    public static WebException conflictException(String message) { return new WebException(CONFLICT, message); }

    public static WebException tooManyException(String message) { return new WebException(TOO_MANY_REQUESTS, message); }

    public static void checkBadRequest(boolean condition, String message) { checkBadRequest(condition, message, null); }

    @SuppressWarnings("UnusedReturnValue")
    public static <T> T checkBadRequest(boolean condition, String message, T r) { if (!condition) { throw badRequestException(message); } return r; }

    public static class NotFoundException extends RuntimeException {
        private NotFoundException(String message) { super(message); }
    }

    public static class WebException extends RuntimeException {
        public final HttpStatus status;

        public WebException(HttpStatus status, String message) { this(status, message, null); }

        public WebException(HttpStatus status, String message, Throwable cause) { super(escapeHtml4(message), cause); this.status = status; }
    }
}