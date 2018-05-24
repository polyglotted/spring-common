package io.polyglotted.spring.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused") @Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
public @interface IsAdmin {}