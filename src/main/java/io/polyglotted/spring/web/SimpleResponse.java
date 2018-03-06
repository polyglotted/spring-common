package io.polyglotted.spring.web;

import io.polyglotted.common.model.MapResult;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static io.polyglotted.common.model.MapResult.immutableResult;
import static java.util.Locale.ENGLISH;

@SuppressWarnings("WeakerAccess") @EqualsAndHashCode(callSuper = true)
public class SimpleResponse extends ResponseEntity<MapResult> {
    public static final SimpleResponse OK = new SimpleResponse(HttpStatus.OK);

    public SimpleResponse(HttpStatus status) {
        this(immutableResult("result", status.getReasonPhrase().toLowerCase(ENGLISH).replace(' ', '-')), status);
    }

    public SimpleResponse(MapResult result) { super(result, HttpStatus.OK); }

    public SimpleResponse(MapResult result, HttpStatus status) { super(result, status); }
}