package io.polyglotted.spring.security;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class DefaultAuthToken extends AbstractAuthenticationToken {
    @Getter private final Object principal;
    @Getter private final Object credentials;

    public DefaultAuthToken(Principal principal, String credentials, Collection<GrantedAuthority> authorities) {
        super(authorities); this.principal = principal; this.credentials = credentials;
    }

    public Principal principal() { return (Principal) principal; }
}