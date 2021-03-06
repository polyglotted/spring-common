package io.polyglotted.spring.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import static io.polyglotted.common.util.ReflectionUtil.isAssignable;

@Component
public class DefaultAuthProvider implements AuthenticationProvider {
    @Override public Authentication authenticate(Authentication authentication) throws AuthenticationException { return authentication; }

    @Override public boolean supports(Class<?> authentication) { return isAssignable(DefaultAuthToken.class, authentication); }
}