package com.igovern.data.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Calls Microservice 2 to enqueue a message when MS1 deletes a record.
 */
@Component
public class AmqClient {

    private static final Logger log = LogManager.getLogger(AmqClient.class);
    private final RestClient http;

    public AmqClient(@Value("${amq-service.base-url}") String baseUrl) {
        this.http = RestClient.builder().baseUrl(baseUrl).build();
    }

    public void notifyDeletion(String entityType, Long entityId) {
        try {
            String message = String.format("DELETE event: %s id=%d", entityType, entityId);
            http.post()
                .uri("/messages/send")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", message))
                .retrieve()
                .toBodilessEntity();
            log.info("Published delete notification to AMQ service: {}", message);
        } catch (Exception ex) {
            // Do not fail the delete if AMQ is offline – just log it.
            log.error("Failed to publish delete notification to AMQ service: {}", ex.getMessage());
        }
    }
}
