package io.polyglotted.spring.web;

import io.polyglotted.common.model.MapResult;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static io.polyglotted.common.model.MapResult.immutableResult;
import static java.util.Locale.ENGLISH;

@SuppressWarnings("WeakerAccess") @EqualsAndHashCode(callSuper = true)
public final class SimpleResponse extends ResponseEntity<MapResult> {
    public static final SimpleResponse OK = new SimpleResponse(HttpStatus.OK);

    public SimpleResponse(HttpStatus status) {
        this(immutableResult("result", status.getReasonPhrase().toLowerCase(ENGLISH).replace(' ', '-')), status);
    }

    public SimpleResponse(String value) { this(immutableResult("result", value)); }

    public SimpleResponse(MapResult result) { this(result, HttpStatus.OK); }

    public SimpleResponse(MapResult result, HttpStatus status) { super(result, status); }
}