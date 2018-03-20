package io.polyglotted.spring.elastic;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.util.HttpUtil;
import lombok.RequiredArgsConstructor;
import org.apache.http.Header;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;

@RequiredArgsConstructor @Component
final class ElasticHandler implements Closeable {
    @Autowired private ObjectMapper objectMapper = null;
    private final CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(RequestConfig.custom()
        .setConnectTimeout(3000).setSocketTimeout(3000).build()).build();

    void simpleGet(String endpoint, Header authHeader) {
    }


    @PreDestroy @Override public void close() throws IOException { httpClient.close(); }
}