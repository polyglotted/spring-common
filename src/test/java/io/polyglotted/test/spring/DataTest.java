package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.polyglotted.common.util.EncodingUtil.safeUrl;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

@RunWith(SpringJUnit4ClassRunner.class)
public class DataTest extends AbstractSpringTest{

    @Test
    public void downloadSuccess() throws Exception {
        AuthenticationResultType loginResult = loginUser(restTemplate, user1);
        try {
            ResponseEntity<String> responseEntity = restTemplate.exchange("/api/download/" + safeUrl("emltest/kmsobj123"), GET,
                buildRequest(null, loginResult.getAccessToken()), String.class);

            assertThat(responseEntity.getStatusCodeValue(), is(OK.value()));
            assertThat(responseEntity.getBody(), is("hello kms"));

        } finally { logout(restTemplate, loginResult); }
    }
}