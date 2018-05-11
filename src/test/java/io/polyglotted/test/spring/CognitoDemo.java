package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import io.polyglotted.aws.common.AwsClientFactory;
import io.polyglotted.aws.config.AwsConfig;
import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.common.util.HttpClientFactory.HttpConfig;
import io.polyglotted.spring.cognito.CognitoConfig;
import io.polyglotted.spring.cognito.CognitoProcessor;
import io.polyglotted.spring.elastic.ElasticProcessor;
import io.polyglotted.spring.security.DefaultAuthToken;
import io.polyglotted.spring.web.SimpleResponse;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.iharder.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import static com.amazonaws.util.IOUtils.copy;
import static io.polyglotted.aws.common.AwsClientFactory.createS3Client;
import static io.polyglotted.aws.common.AwsContentUtil.contentTypeMetaData;
import static io.polyglotted.aws.common.AwsContentUtil.fetchContentType;
import static io.polyglotted.aws.common.S3Fetcher.fetchMayBeSecure;
import static io.polyglotted.aws.common.S3Fetcher.fetchObjectMetadata;
import static io.polyglotted.common.model.MapResult.immutableResult;
import static io.polyglotted.common.util.EncodingUtil.encodeBase64;
import static io.polyglotted.common.util.EncodingUtil.urlDecode;
import static io.polyglotted.common.util.StrUtil.safeLastSuffix;

@SuppressWarnings("unused") @ComponentScan({"io.polyglotted.spring"})
@EnableEncryptableProperties @SpringBootApplication
public class CognitoDemo {
    @Bean @ConfigurationProperties("aws")
    public AwsConfig awsConfig() { return new AwsConfig(); }

    @Bean @ConfigurationProperties("aws.cognito")
    public CognitoConfig cognitoConfig() { return new CognitoConfig(); }

    @Bean(name = "idpEsHttpConfig") @ConfigurationProperties("idp.es.http")
    public HttpConfig httpConfig() { return new HttpConfig().setScheme("https"); }

    @Bean(name = "integrationUser1") @ConfigurationProperties("spc.intg.user")
    public IntegrationUser integrationUser1() { return new IntegrationUser(); }

    @Bean(name = "integrationUser2") @ConfigurationProperties("spc.intg.user2")
    public IntegrationUser integrationUser2() { return new IntegrationUser(); }

    @Bean public AWSCognitoIdentityProvider cognitoClient(AwsConfig config) { return AwsClientFactory.cognitoClient(config); }

    public static void main(String args[]) { SpringApplication.run(CognitoDemo.class, args); }

    @NoArgsConstructor @Getter @Setter
    static class IntegrationUser {
        private String email;
        private String password;

        IntegrationUser(String email, String passwd) { this.email = email; this.password = passwd; }

        String basicHeader() { return "Basic " + encodeBase64((email + ":" + password).getBytes()); }
    }

    @RestController static class SampleController {
        @PreAuthorize("hasRole('ROLE_CONSUMER') or hasRole('ROLE_CURATOR')")
        @GetMapping(path = "/api/sample", produces = "application/json")
        public SimpleResponse sample(Principal subject) { return SimpleResponse.OK; }

        @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
        @GetMapping(path = "/api/sample-admin", produces = "application/json")
        public SimpleResponse sampleAdmin(DefaultAuthToken token) { return new SimpleResponse(immutableResult("result", "admin")); }
    }

    @RestController static class DataController {
        private static final String BUCKET = "steeleye-sdk-java.steeleye.co";
        @Autowired private AwsConfig awsConfig = null;

        @PreAuthorize("hasRole('ROLE_CONSUMER')") @GetMapping(path = "/api/download/{key}")
        public void download(HttpServletResponse response, @PathVariable("key") String keyStr,
                             @RequestParam(name = "base64", defaultValue = "false") boolean base64) throws IOException {
            String key = urlDecode(keyStr);
            ObjectMetadata metadata = fetchObjectMetadata(awsConfig, BUCKET, key);
            String contentType = fetchContentType(metadata, immutableResult(), key);
            InputStream baseStream = fetchMayBeSecure(awsConfig, BUCKET, key, metadata);
            try (InputStream inputStream = base64 ? new Base64.InputStream(baseStream) : baseStream) {

                response.addHeader("Content-Disposition", "attachment;filename=\"" + safeLastSuffix(key, "/") + "\"");
                response.setContentType(contentType);
                copy(inputStream, response.getOutputStream());
                response.flushBuffer();
            }
        }

        @PreAuthorize("hasRole('ROLE_CURATOR')") @PutMapping(path = "/api/put/{id}")
        public SimpleResponse put(@PathVariable("id") String id, @RequestBody SimpleMapResult mapResult) { return new SimpleResponse(mapResult); }

        @PreAuthorize("hasRole('ROLE_CURATOR')") @PostMapping(path = "/api/upload/{key}")
        public SimpleResponse upload(HttpServletRequest request, @PathVariable("key") String keyStr) throws IOException {
            createS3Client(awsConfig).putObject(BUCKET, urlDecode(keyStr), request.getInputStream(),
                contentTypeMetaData(keyStr, request.getContentLengthLong()));
            return SimpleResponse.OK;
        }

        @PreAuthorize("hasRole('ROLE_CURATOR')") @DeleteMapping(path = "/api/delete/{key}")
        public SimpleResponse delete(HttpServletRequest request, @PathVariable("key") String keyStr) throws IOException {
            createS3Client(awsConfig).deleteObject(BUCKET, urlDecode(keyStr));
            return SimpleResponse.OK;
        }
    }

    @RestController static class CognitoLoginController {
        @Autowired private CognitoProcessor cognitoProcessor = null;

        @PostMapping(path = "/cognito/login", params = {"email", "password"}, produces = "application/json")
        public AuthenticationResultType login(String email, String password) throws IOException { return cognitoProcessor.login(email, password); }

        @GetMapping(path = "/cognito/logout", produces = "application/json")
        public SimpleResponse logout(DefaultAuthToken token) throws IOException { return cognitoProcessor.logout(token.logoutToken()); }
    }

    @RestController static class ElasticLoginController {
        @Autowired private ElasticProcessor elasticProcessor = null;

        @PostMapping(path = "/elastic/login", params = {"userId", "password"}, produces = "application/json")
        public AuthToken login(String userId, String password) throws IOException { return elasticProcessor.login(userId, password); }

        @GetMapping(path = "/elastic/logout", produces = "application/json")
        public SimpleResponse logout(DefaultAuthToken token) throws IOException { return elasticProcessor.logout(token.logoutToken()); }
    }
}