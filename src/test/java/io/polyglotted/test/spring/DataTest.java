package io.polyglotted.test.spring;

import com.amazonaws.services.cognitoidp.model.AuthenticationResultType;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.polyglotted.common.util.EncodingUtil.urlEncode;
import static io.polyglotted.common.util.ResourceUtil.readResourceBytes;
import static io.polyglotted.common.util.ThreadUtil.safeSleep;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

@RunWith(SpringJUnit4ClassRunner.class)
public class DataTest extends AbstractSpringTest {

    @Test
    public void downloadSuccess() throws Exception {
        AuthenticationResultType loginResult = loginUser(user1);
        try {
            ResponseEntity<String> stringResp = doGet("/api/download/" + urlEncode("emltest/kmsobj123"), loginResult, String.class);
            assertThat(stringResp.getBody(), is("hello kms"));

            stringResp = doGet("/api/download/" + urlEncode("emltest/nokms.txt"), loginResult, String.class);
            assertThat(stringResp.getBody(), is("no kms id"));

            assertThat(requireNonNull(downloadEntity(loginResult, "emltest/largemp3.mp3", "audio/mp3").getBody()).length, is(443926));
        } finally { logout(loginResult); }
    }

    @Test
    public void awsLifeCycleTest() throws Exception {
        AuthenticationResultType loginResult = loginUser(user1);
        try {
            awsLifeCycle(loginResult, "feeds/tiny.jpg", "image/jpeg");
            awsLifeCycle(loginResult, "feeds/testpdf.zip", "application/zip");

        } finally { logout(loginResult); }
    }

    private void awsLifeCycle(AuthenticationResultType loginResult, String keyPath, String contentType) {
        byte[] expected = readResourceBytes(DataTest.class, keyPath);
        try {
            doPost("/api/upload/" + urlEncode(keyPath), loginResult, expected, SimpleMapResult.class);
            safeSleep(100);
        } catch (Exception ex) { throw new AssertionFailedError(ex.getMessage()); }
        try {
            assertThat(downloadEntity(loginResult, keyPath, contentType).getBody(), is(expected));
        } catch (Exception ex) { throw new AssertionFailedError(ex.getMessage()); }
        try {
            doDelete("/api/delete/" + urlEncode(keyPath), loginResult);
            safeSleep(100);
        } catch (Exception ex) { throw new AssertionFailedError(ex.getMessage()); }
    }

    private ResponseEntity<byte[]> downloadEntity(AuthenticationResultType loginResult, String path, String contentType) {
        ResponseEntity<byte[]> responseEntity = doGet("/api/download/" + urlEncode(path), loginResult, byte[].class);
        assertThat(responseEntity.getHeaders().getFirst(CONTENT_TYPE), is(contentType));
        return responseEntity;
    }
}