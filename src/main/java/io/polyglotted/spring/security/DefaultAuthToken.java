package io.polyglotted.spring.security;

import io.polyglotted.common.model.Subject;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class DefaultAuthToken extends AbstractAuthenticationToken {
    @Getter private final Object principal;
    @Getter private final Object credentials;

    public DefaultAuthToken(Subject subject, String credentials, Collection<GrantedAuthority> authorities) {
        super(authorities); this.principal = subject; this.credentials = credentials;
    }

    @SuppressWarnings("unused") public Subject subject() { return (Subject) principal; }
}