package com.myfitpet.chat;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatModelClient chatModelClient;

    public ChatController(ChatModelClient chatModelClient) {
        this.chatModelClient = chatModelClient;
    }

    static class ChatRequestDto {
        public String query;
        public List<List<String>> history;
    }

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequestDto req) {
        if (req == null || req.query == null) {
            return ResponseEntity.badRequest().body("missing query");
        }
        List<List<String>> history = req.history != null ? req.history : List.of();
        ChatResponse resp = chatModelClient.chat(req.query, history);
        return ResponseEntity.ok(resp);
    }
}
