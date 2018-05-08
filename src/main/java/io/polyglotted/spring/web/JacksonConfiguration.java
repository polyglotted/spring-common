package io.polyglotted.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.polyglotted.common.util.BaseSerializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused") @Slf4j @Configuration
public class JacksonConfiguration {

    @Autowired public void configeJackson(ObjectMapper objectMapper) {
        BaseSerializer.configureMapper(objectMapper);
        log.info("BaseSerializer configured");
    }
}