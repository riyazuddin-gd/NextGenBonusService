package com.example.nextgenBonus.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())        // support LocalDateTime
            .registerModule(new Hibernate6Module())      // support Hibernate lazy collections
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO-8601 instead of timestamps

    public static String toJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting to JSON", e);
        }
    }
}
