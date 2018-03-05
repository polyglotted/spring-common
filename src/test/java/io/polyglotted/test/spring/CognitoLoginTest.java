package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.polyglotted.common.util.ListBuilder.immutableList;
import static io.polyglotted.common.util.MapRetriever.reqdStr;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CognitoDemo.class)
public class CognitoLoginTest {
    @Autowired @Qualifier("integrationUser1") private IntegrationUser user1 = null;
    @Autowired @Qualifier("integrationUser2") private IntegrationUser user2 = null;
    @Autowired private TestRestTemplate restTemplate = null;

    @Test
    public void loginFailure() throws Exception {
        String url = "/cognito/login?email=&password=fooBarBaz";
        ResponseEntity<SimpleMapResult> responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, BAD_REQUEST, "message", "Invalid credentials.");

        url = "/cognito/login?email=" + user1.getEmail() + "&password=fooBarBaz";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "Incorrect username or password.");

        url = "/cognito/login?email=foo@fooz.com&password=fooBarBaz";
        responseEntity = restTemplate.postForEntity(url, "{}", SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "User does not exist.");
    }

    @Test
    public void authorizationFailure() throws Exception {
        AuthenticationResultType loginResult = loginUser(restTemplate, user2);

        ResponseEntity<SimpleMapResult> responseEntity = restTemplate.exchange("/api/sample", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "ok");

        responseEntity = restTemplate.exchange("/api/sample-admin", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, FORBIDDEN, "message", "User is not authorised to perform action.");

        responseEntity = restTemplate.postForEntity("/cognito/logout", loginResult, SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "logged-out");

        responseEntity = restTemplate.exchange("/api/sample", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, UNAUTHORIZED, "message", "Unauthorized");
    }

    @Test
    public void loginLogoutSuccess() throws Exception {
        AuthenticationResultType loginResult = loginUser(restTemplate, user1);

        ResponseEntity<SimpleMapResult> responseEntity = restTemplate.exchange("/api/sample-admin", GET,
            buildRequest(null, loginResult.getAccessToken()), SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "admin");

        responseEntity = restTemplate.postForEntity("/cognito/logout", loginResult, SimpleMapResult.class);
        assertEntity(responseEntity, OK, "result", "logged-out");
    }

    private static AuthenticationResultType loginUser(TestRestTemplate restTemplate, IntegrationUser user) {
        String url = "/cognito/login?email=" + user.getEmail() + "&password=" + user.getPassword();
        AuthenticationResultType loginResult = restTemplate.postForObject(url, "{}", AuthenticationResultType.class);
        assertThat(loginResult, is(notNullValue()));
        assertThat(loginResult.getAccessToken(), is(notNullValue()));
        return loginResult;
    }

    private static <T> HttpEntity<T> buildRequest(T body, String accessToken) {
        HttpHeaders headers = new HttpHeaders(); headers.setAccept(immutableList(MediaType.APPLICATION_JSON));
        if (accessToken != null) { headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken); }
        return new HttpEntity<>(body, headers);
    }

    private static void assertEntity(ResponseEntity<SimpleMapResult> responseEntity, HttpStatus status, String prop, String message) {
        assertThat(responseEntity.getStatusCodeValue(), is(status.value()));
        SimpleMapResult result = responseEntity.getBody();
        assertThat(result, is(notNullValue()));
        assertThat(reqdStr(requireNonNull(result), prop), is(message));
    }
}