package io.polyglotted.spring.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@NoArgsConstructor @Getter @Setter
public final class AccessKey {
    @NotNull private String accessToken;
    private Integer expiresIn;
    private String tokenType;
}