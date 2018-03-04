package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import io.polyglotted.test.spring.CognitoDemo.IntegrationUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT, classes = CognitoDemo.class)
public class CognitoLoginTest {
    @Autowired private IntegrationUser user = null;
    @Autowired private TestRestTemplate restTemplate = null;

    @Test
    public void loginLogout() throws Exception {
        String url = "/cognito/login?email=" + user.getEmail() + "&password=" + user.getPassword();
        AuthenticationResultType loginResult = restTemplate.postForObject(url, "{}", AuthenticationResultType.class);
        assertThat(loginResult, is(notNullValue()));
        assertThat(loginResult.getAccessToken(), is(notNullValue()));

        MapResult logoutResult = restTemplate.postForObject("/cognito/logout", loginResult, SimpleMapResult.class);
        assertThat(logoutResult, is(notNullValue()));
        assertThat(logoutResult.get("result"), is("logged-out"));
    }
}