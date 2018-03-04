package io.polyglotted.spring.errorhandling;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.ConstraintViolation;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.WRAPPER_OBJECT;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.CUSTOM;
import static io.polyglotted.common.util.NullUtil.nonNullFn;

@Data @RequiredArgsConstructor @NotThreadSafe
@JsonTypeInfo(include = WRAPPER_OBJECT, use = CUSTOM, property = "error", visible = true)
@JsonTypeIdResolver(LowerCaseClassNameResolver.class) class ApiError {
    private final HttpStatus status;
    @JsonFormat(shape = STRING, pattern = "dd-MM-yyyy hh:mm:ss")
    private final LocalDateTime timestamp;
    private final String message;
    private final String debugMessage;
    private final List<ApiSubError> subErrors = new LinkedList<>();

    ApiError(HttpStatus status) { this(status, "Unexpected error", null); }

    ApiError(HttpStatus status, String message) { this(status, message, null); }

    ApiError(HttpStatus status, Throwable ex) { this(status, "Unexpected error", null); }

    ApiError(HttpStatus status, String message, Throwable ex) {
        this(status, LocalDateTime.now(), message, nonNullFn(ex, Throwable::getLocalizedMessage, null));
    }

    private void addValidationError(String object, String field, Object rejectedValue, String message) {
        subErrors.add(new ApiValidationError(object, field, rejectedValue, message));
    }

    private void addValidationError(String object, String message) { subErrors.add(new ApiValidationError(object, message)); }

    ApiError withFieldErrors(List<FieldError> fieldErrors) { fieldErrors.forEach(this::addValidationError); return this; }

    ApiError withObjectErrors(List<ObjectError> globalErrors) { globalErrors.forEach(this::addValidationError); return this; }

    ApiError withViolations(Set<ConstraintViolation<?>> violations) { violations.forEach(this::addValidationError); return this; }

    private void addValidationError(FieldError fieldError) {
        this.addValidationError(fieldError.getObjectName(), fieldError.getField(), fieldError.getRejectedValue(), fieldError.getDefaultMessage());
    }

    private void addValidationError(ObjectError objectError) {
        this.addValidationError(objectError.getObjectName(), objectError.getDefaultMessage());
    }

    private void addValidationError(ConstraintViolation<?> cv) {
        this.addValidationError(cv.getRootBeanClass().getSimpleName(), ((PathImpl) cv.getPropertyPath()).getLeafNode().asString(),
            cv.getInvalidValue(), cv.getMessage());
    }

    abstract class ApiSubError {
        //MARKER
    }

    @Data
    @EqualsAndHashCode(callSuper = false)
    @AllArgsConstructor class ApiValidationError extends ApiSubError {
        private final String object;
        private final String field;
        private final Object rejectedValue;
        private final String message;

        ApiValidationError(String object, String message) { this(object, null, null, message); }
    }
}

class LowerCaseClassNameResolver extends TypeIdResolverBase {
    @Override public String idFromValue(Object value) { return value.getClass().getSimpleName().toLowerCase(); }

    @Override public String idFromValueAndType(Object value, Class<?> suggestedType) { return idFromValue(value); }

    @Override public JsonTypeInfo.Id getMechanism() { return CUSTOM; }
}