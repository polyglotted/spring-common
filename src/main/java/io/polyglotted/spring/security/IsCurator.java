package io.polyglotted.spring.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressWarnings("unused") @Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAnyRole('ROLE_ADMINISTRATOR','ROLE_CURATOR','ROLE_GATE_KEEPER','ROLE_MODELER')")
public @interface IsCurator {}