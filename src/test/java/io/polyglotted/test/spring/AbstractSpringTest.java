package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static io.polyglotted.common.util.ListBuilder.immutableList;
import static io.polyglotted.common.util.MapRetriever.reqdStr;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CognitoDemo.class)
abstract class AbstractSpringTest {
    @Autowired @Qualifier("integrationUser1") IntegrationUser user1 = null;
    @Autowired @Qualifier("integrationUser2") IntegrationUser user2 = null;
    @Autowired TestRestTemplate restTemplate = null;

    static AuthenticationResultType loginUser(TestRestTemplate restTemplate, IntegrationUser user) {
        String url = "/cognito/login?email=" + user.getEmail() + "&password=" + user.getPassword();
        AuthenticationResultType loginResult = restTemplate.postForObject(url, "{}", AuthenticationResultType.class);
        assertThat(loginResult, is(notNullValue()));
        assertThat(loginResult.getAccessToken(), is(notNullValue()));
        return loginResult;
    }

    static void logout(TestRestTemplate restTemplate, AuthenticationResultType loginResult) {
        ResponseEntity<SimpleMapResult> responseEntity;
        responseEntity = restTemplate.postForEntity("/cognito/logout", loginResult, SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "logged-out");
    }

    static <T> HttpEntity<T> buildRequest(T body, String accessToken) {
        HttpHeaders headers = new HttpHeaders(); headers.setAccept(immutableList(MediaType.APPLICATION_JSON));
        if (accessToken != null) { headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken); }
        return new HttpEntity<>(body, headers);
    }

    static void assertEntity(ResponseEntity<SimpleMapResult> responseEntity, HttpStatus status, String prop, String message) {
        assertThat(responseEntity.getStatusCodeValue(), is(status.value()));
        SimpleMapResult result = responseEntity.getBody();
        assertThat(result, is(notNullValue()));
        assertThat(reqdStr(requireNonNull(result), prop), is(message));
    }
}