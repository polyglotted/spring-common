package io.polyglotted.spring.security;

import io.polyglotted.common.model.Subject;
import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@SuppressWarnings({"unused", "WeakerAccess"})
public class DefaultAuthToken extends AbstractAuthenticationToken {
    @Getter private final Object principal;
    @Getter private final Object credentials;

    public DefaultAuthToken(Subject subject, String credentials, Collection<GrantedAuthority> authorities) {
        super(authorities); super.setAuthenticated(true); this.principal = subject; this.credentials = credentials;
    }

    public String userId() { return subject().username; }

    public Subject subject() { return (Subject) principal; }

    public String logoutToken() { return ((String) credentials).substring(7); }
}