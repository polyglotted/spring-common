package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.model.AWSCognitoIdentityProviderException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RequiredArgsConstructor
public class CognitoAuthFilter extends OncePerRequestFilter {
    private final CognitoProcessor cognitoProcessor;

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        Authentication authentication;
        try {
            System.out.println("\n>> COGNITO FILTER APPLIED");
            authentication = cognitoProcessor.authenticate(request);
            if (authentication != null) { SecurityContextHolder.getContext().setAuthentication(authentication); }

        } catch (AWSCognitoIdentityProviderException ex) {
            logger.error("IDP Error processing Bearer token " + ex.getMessage());
            SecurityContextHolder.clearContext();

        } catch (Exception ex) { logger.error("Unknown Error processing Bearer token", ex); }
        filterChain.doFilter(request, response);
    }
}