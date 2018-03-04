package io.polyglotted.spring.cognito;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public abstract class AbstractCognito {
    protected final CognitoConfig config;
    protected final AWSCognitoIdentityProvider cognitoClient;

    @NoArgsConstructor @Getter @Setter @Accessors(chain = true)
    public static class CognitoConfig {
        private String userPoolId;
        private String clientId;
        private String clientSecret;
    }
}