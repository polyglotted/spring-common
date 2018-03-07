package io.polyglotted.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.util.BaseSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused") @Configuration
public class JacksonConfiguration {

    @Autowired public void configeJackson(ObjectMapper objectMapper) {
        BaseSerializer.configureMapper(objectMapper);
    }
}