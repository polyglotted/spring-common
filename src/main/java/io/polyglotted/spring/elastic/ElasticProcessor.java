package io.polyglotted.spring.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.spring.security.DefaultAuthToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

import static org.apache.http.HttpHeaders.AUTHORIZATION;

@Slf4j @Component
public class ElasticProcessor {
    @Autowired private ElasticHandler elasticHandler = null;

    DefaultAuthToken authenticate(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION);
        if (header != null && header.startsWith("Bearer")) {
            String bearerToken = header.substring(7);
        }
        log.trace("No Bearer token found in HTTP Authorization header");
        return null;
    }
}