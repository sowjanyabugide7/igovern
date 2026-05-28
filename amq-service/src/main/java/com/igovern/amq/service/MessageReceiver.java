package com.igovern.amq.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MessageReceiver {

    private static final Logger log = LogManager.getLogger(MessageReceiver.class);

    /** Recent messages echoed by the receiver, exposed via REST for verification. */
    private final List<String> echoed = Collections.synchronizedList(new ArrayList<>());

    @JmsListener(destination = "${igovern.amq.queue}")
    public void onMessage(String message) {
        String echoed = "ECHO: " + message;
        log.info("Receiver got message: {}", message);
        log.info("{}", echoed);
        this.echoed.add(echoed);
        // Cap memory usage – keep only the last 100 messages
        if (this.echoed.size() > 100) {
            this.echoed.remove(0);
        }
    }

    public List<String> getEchoed() {
        synchronized (echoed) {
            return new ArrayList<>(echoed);
        }
    }
}
