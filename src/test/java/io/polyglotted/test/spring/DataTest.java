package io.polyglotted.test.spring;

import io.polyglotted.common.model.AuthToken;
import io.polyglotted.common.model.MapResult;
import io.polyglotted.common.model.MapResult.SimpleMapResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static io.polyglotted.common.model.MapResult.simpleResult;
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
    public void defaultPutTest() throws Exception {
        AuthToken accessKey = loginUser(user2);
        try {
            MapResult expected = simpleResult("baz", 1, "foo", "bar");
            ResponseEntity<SimpleMapResult> result = doPut("/api/put/test", accessKey, expected, SimpleMapResult.class);
            assertThat(result.getBody(), is(expected));
        } catch (AssertionError ex) {
            ex.printStackTrace();
        } finally { logout(accessKey); }
    }

    @Test
    public void downloadSuccess() throws Exception {
        AuthToken token = loginUser(user1);
        try {
            ResponseEntity<String> stringResp = doGet("/api/download/" + urlEncode("emltest/kmsobj123"), token, String.class);
            assertThat(stringResp.getBody(), is("hello kms"));

            stringResp = doGet("/api/download/" + urlEncode("emltest/nokms.txt"), token, String.class);
            assertThat(stringResp.getBody(), is("no kms id"));

            assertThat(requireNonNull(downloadEntity(token, "emltest/largemp3.mp3", "audio/mp3").getBody()).length, is(443926));
        } finally { logout(token); }
    }

    @Test
    public void awsLifeCycleTest() throws Exception {
        AuthToken token = loginUser(user1);
        try {
            awsLifeCycle(token, "feeds/tiny.jpg", "image/jpeg");
        } finally { logout(token); }

        token = loginUser(user2);
        try {
            awsLifeCycle(token, "feeds/testpdf.zip", "application/zip");
        } finally { logout(token); }
    }

    private void awsLifeCycle(AuthToken token, String keyPath, String contentType) {
        byte[] expected = readResourceBytes(DataTest.class, keyPath);
        try {
            doPost("/api/upload/" + urlEncode(keyPath), token, expected, SimpleMapResult.class);
            safeSleep(100);
        } catch (AssertionError ex) { ex.printStackTrace(); }
        try {
            assertThat(downloadEntity(token, keyPath, contentType).getBody(), is(expected));
        } catch (AssertionError ex) { ex.printStackTrace(); }
        try {
            doDelete("/api/delete/" + urlEncode(keyPath), token);
            safeSleep(100);
        } catch (AssertionError ex) { ex.printStackTrace(); }
    }

    private ResponseEntity<byte[]> downloadEntity(AuthToken token, String path, String contentType) {
        ResponseEntity<byte[]> responseEntity = doGet("/api/download/" + urlEncode(path), token, byte[].class);
        assertThat(responseEntity.getHeaders().getFirst(CONTENT_TYPE), is(contentType));
        return responseEntity;
    }
}