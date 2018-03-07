package io.polyglotted.test.spring;

import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.spring.security.AccessKey;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import junit.framework.AssertionFailedError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CognitoDemo.class)
abstract class AbstractSpringTest {
    @Autowired @Qualifier("integrationUser1") IntegrationUser user1 = null;
    @Autowired @Qualifier("integrationUser2") IntegrationUser user2 = null;
    @Autowired TestRestTemplate restTemplate = null;

    AccessKey loginUser(IntegrationUser user) {
        String url = "/cognito/login?email=" + user.getEmail() + "&password=" + user.getPassword();
        AccessKey accessKey = doPost(url, null, null, AccessKey.class).getBody();
        assertThat(requireNonNull(accessKey).getAccessToken(), is(notNullValue()));
        return accessKey;
    }

    void logout(AccessKey accessKey) {
        assertEntity(doPost("/cognito/logout", null, accessKey, SimpleMapResult.class), OK, "result", "logged-out");
    }

    <T> ResponseEntity<T> doGet(String url, AccessKey key, Class<T> clazz) {
        return checkSuccess(execute(url, GET, key, null, clazz));
    }

    <T> ResponseEntity<T> doPost(String url, AccessKey key, Object body, Class<T> clazz) {
        return checkSuccess(execute(url, POST, key, body, clazz));
    }

    <T> ResponseEntity<T> doPut(String url, AccessKey key, Object body, Class<T> clazz) {
        return checkSuccess(execute(url, PUT, key, body, clazz));
    }

    void doDelete(String url, AccessKey key) { checkSuccess(execute(url, DELETE, key, null, SimpleMapResult.class)); }

    <T> ResponseEntity<T> execute(String url, HttpMethod method, AccessKey key, Object body, Class<T> clazz) {
        HttpHeaders headers = new HttpHeaders(); headers.setAccept(immutableList(MediaType.APPLICATION_JSON));
        if (key != null) { headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + key.getAccessToken()); }
        return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), clazz);
    }

    private static <T> ResponseEntity<T> checkSuccess(ResponseEntity<T> responseEntity) {
        if (responseEntity.getStatusCodeValue() != OK.value()) {
            throw new AssertionFailedError(responseEntity.getStatusCode().getReasonPhrase() + ":" + String.valueOf(responseEntity.getBody()));
        }
        return responseEntity;
    }

    static void assertEntity(ResponseEntity<SimpleMapResult> responseEntity, HttpStatus status, String prop, String message) {
        assertThat(responseEntity.getStatusCodeValue(), is(status.value()));
        SimpleMapResult result = responseEntity.getBody();
        assertThat(result, is(notNullValue()));
        assertThat(reqdStr(requireNonNull(result), prop), is(message));
    }
}