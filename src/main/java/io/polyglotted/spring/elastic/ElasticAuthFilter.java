package io.polyglotted.spring.elastic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j @RequiredArgsConstructor
public class ElasticAuthFilter extends OncePerRequestFilter {
    private final ElasticProcessor elasticProcessor;

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Authentication authentication;
        try {
            authentication = elasticProcessor.authenticate(request);
            if (authentication != null) { SecurityContextHolder.getContext().setAuthentication(authentication); }

        } catch (ElasticClientException ex) {
            SecurityContextHolder.clearContext();

        } catch (Exception ex) { log.error("Unknown Error processing Bearer token", ex); }
        filterChain.doFilter(request, response);
    }

    static class ElasticClientException extends RuntimeException {
        ElasticClientException(String message) { super(message); }
    }
}