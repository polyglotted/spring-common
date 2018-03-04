package io.polyglotted.spring.errorhandling;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.unbescape.html.HtmlEscape.escapeHtml4;

@SuppressWarnings({"unused", "WeakerAccess", "Serial"})
public abstract class ExceptionFactory {

    public static NotFoundException notFound(Class<?> clazz, String... paramsMap) { return new NotFoundException(clazz, paramsMap); }

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

    static class NotFoundException extends RuntimeException {
        NotFoundException(Class<?> clazz, String... searchParamsMap) {
            super(NotFoundException.generateMessage(clazz.getSimpleName(), toMap(String.class, String.class, searchParamsMap)));
        }

        private static String generateMessage(String entity, Map<String, String> searchParams) {
            return StringUtils.capitalize(entity) + " was not found for parameters " + searchParams;
        }

        private static <K, V> Map<K, V> toMap(Class<K> keyType, Class<V> valueType, String... entries) {
            if (entries.length % 2 == 1) throw new IllegalArgumentException("Invalid entries");
            return IntStream.range(0, entries.length / 2).map(i -> i * 2).collect(LinkedHashMap::new,
                (m, i) -> m.put(keyType.cast(entries[i]), valueType.cast(entries[i + 1])), Map::putAll);
        }
    }

    static class WebException extends RuntimeException {
        public final HttpStatus status;

        public WebException(HttpStatus status, String message) { this(status, message, null); }

        public WebException(HttpStatus status, String message, Throwable cause) { super(escapeHtml4(message), cause); this.status = status; }
    }
}