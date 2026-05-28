package com.igovern.amq.controller;

import com.igovern.amq.service.MessageProducer;
import com.igovern.amq.service.MessageReceiver;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageProducer producer;
    private final MessageReceiver receiver;

    public MessageController(MessageProducer producer, MessageReceiver receiver) {
        this.producer = producer;
        this.receiver = receiver;
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> send(@Valid @RequestBody SendRequest req) {
        producer.send(req.getMessage());
        return ResponseEntity.ok(Map.of("status", "queued", "message", req.getMessage()));
    }

    @GetMapping("/echoed")
    public List<String> echoed() {
        return receiver.getEchoed();
    }

    public static class SendRequest {
        @NotBlank
        @Size(max = 500)
        private String message;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
