package io.polyglotted.spring.errorhandling;

import io.polyglotted.common.util.MapBuilder;
import io.polyglotted.spring.errorhandling.ExceptionFactory.NotFoundException;
import io.polyglotted.spring.errorhandling.ExceptionFactory.WebException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j @Order(HIGHEST_PRECEDENCE) @ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(NotFoundException ex, WebRequest request) {
        return resultEntity(ex.getMessage(), NOT_FOUND, request);
    }

    @ExceptionHandler(WebException.class)
    protected ResponseEntity<Object> handleWebException(WebException ex, WebRequest request) {
        return resultEntity(ex.getMessage(), ex.status, request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return resultEntity("User is not authorised to perform action.", FORBIDDEN, request);
    }

    private static ResponseEntity<Object> resultEntity(String message, HttpStatus status, WebRequest request) {
        return new ResponseEntity<>(MapBuilder.immutableMapBuilder().put("timestamp", new Date())
            .put("status", status.value()).put("error", status.getReasonPhrase()).put("message", message)
            .put("path", ((ServletWebRequest) request).getRequest().getServletPath()).result(), status);
    }
}