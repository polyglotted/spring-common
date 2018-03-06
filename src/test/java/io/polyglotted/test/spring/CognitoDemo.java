package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProviderClient;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.polyglotted.aws.config.AwsConfig;
import io.polyglotted.aws.config.CredsProvider;
import io.polyglotted.spring.cognito.AbstractCognito.CognitoConfig;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static io.polyglotted.common.model.MapResult.immutableResult;

@SuppressWarnings("unused")
@ComponentScan({"io.polyglotted.spring"})
@EnableEncryptableProperties
@SpringBootApplication
public class CognitoDemo {
    @Bean @ConfigurationProperties("aws")
    public AwsConfig awsConfig() { return new AwsConfig(); }

    @Bean @ConfigurationProperties("aws.cognito")
    public CognitoConfig cognitoConfig() { return new CognitoConfig(); }

    @Bean(name = "integrationUser1") @ConfigurationProperties("spc.intg.user")
    public IntegrationUser integrationUser1() { return new IntegrationUser(); }

    @Bean(name = "integrationUser2") @ConfigurationProperties("spc.intg.user2")
    public IntegrationUser integrationUser2() { return new IntegrationUser(); }

    @Bean public AWSCognitoIdentityProvider cognitoClient(AwsConfig config) {
        return AWSCognitoIdentityProviderClient.builder().withCredentials(CredsProvider.getProvider(config)).withRegion(config.regions()).build();
    }

    public static void main(String args[]) { SpringApplication.run(CognitoDemo.class, args); }

    @Controller
    static class SampleController {
        @PreAuthorize("hasRole('ROLE_CONSUMER') or hasRole('ROLE_CURATOR')")
        @GetMapping(path = "/api/sample", produces = "application/json")
        @ResponseBody public SimpleResponse sample() { return SimpleResponse.OK; }

        @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
        @GetMapping(path = "/api/sample-admin", produces = "application/json")
        @ResponseBody public SimpleResponse sampleAdmin() { return new SimpleResponse(immutableResult("result", "admin")); }
    }

    @NoArgsConstructor @Getter @Setter
    static class IntegrationUser {
        private String email;
        private String password;
    }
}