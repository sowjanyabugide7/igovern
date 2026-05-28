package com.igovern.amq.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

@Service
public class MessageProducer {

    private static final Logger log = LogManager.getLogger(MessageProducer.class);
    private final JmsTemplate jmsTemplate;
    private final String queue;

    public MessageProducer(JmsTemplate jmsTemplate,
                           @Value("${igovern.amq.queue}") String queue) {
        this.jmsTemplate = jmsTemplate;
        this.queue = queue;
    }

    public void send(String message) {
        log.info("Producer sending message to queue '{}': {}", queue, message);
        jmsTemplate.convertAndSend(queue, message);
    }
}
