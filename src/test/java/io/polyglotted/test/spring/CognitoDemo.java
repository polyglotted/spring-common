package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.polyglotted.aws.config.AwsConfig;
import io.polyglotted.aws.config.CredsProvider;
import io.polyglotted.spring.cognito.AbstractCognito.CognitoConfig;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@SuppressWarnings("unused")
@ComponentScan({"io.polyglotted.spring"})
@EnableEncryptableProperties
@SpringBootApplication
public class CognitoDemo {
    @Bean @ConfigurationProperties("aws")
    public AwsConfig awsConfig() { return new AwsConfig(); }

    @Bean @ConfigurationProperties("aws.cognito")
    public CognitoConfig cognitoConfig() { return new CognitoConfig(); }

    @Bean @ConfigurationProperties("spc.intg.user")
    public IntegrationUser integrationUser() { return new IntegrationUser(); }

    @Bean public AWSCognitoIdentityProvider cognitoClient(AwsConfig config) {
        return AWSCognitoIdentityProviderClient.builder().withCredentials(CredsProvider.getProvider(config)).withRegion(config.regions()).build();
    }

    public static void main(String args[]) { SpringApplication.run(CognitoDemo.class, args); }

    @NoArgsConstructor @Getter @Setter
    static class IntegrationUser {
        private String email;
        private String password;
    }
}