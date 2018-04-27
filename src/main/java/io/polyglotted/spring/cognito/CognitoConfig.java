package io.polyglotted.spring.cognito;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@NoArgsConstructor @Getter @Setter @Accessors(chain = true)
public class CognitoConfig {
    private String userPoolId;
    private String clientId;
    private String clientSecret;
}
