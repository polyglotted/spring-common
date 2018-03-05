package io.polyglotted.spring.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker.Std;
import io.polyglotted.common.model.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT;
import static com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY;
import static com.fasterxml.jackson.databind.DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_ENUMS_USING_TO_STRING;
import static com.fasterxml.jackson.databind.SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS;

@SuppressWarnings("unused") @Configuration
public class JacksonConfiguration {

    @Autowired public void configeJackson(ObjectMapper objectMapper) {
        objectMapper.configure(ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        objectMapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        objectMapper.configure(ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false);
        objectMapper.configure(FAIL_ON_NULL_FOR_PRIMITIVES, true);
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(ORDER_MAP_ENTRIES_BY_KEYS, true);
        objectMapper.configure(READ_ENUMS_USING_TO_STRING, true);
        objectMapper.setSerializationInclusion(NON_NULL);
        objectMapper.setVisibility(new Std(NONE, NONE, NONE, ANY, ANY));
        objectMapper.registerModule(Serializers.baseModule());
    }
}