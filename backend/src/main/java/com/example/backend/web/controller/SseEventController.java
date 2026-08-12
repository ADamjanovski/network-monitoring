package com.example.backend.web.controller;

import com.example.backend.service.stream.SseEventService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/events")
public class SseEventController {

    private final SseEventService sseEventService;

    public SseEventController(SseEventService sseEventService) {
        this.sseEventService = sseEventService;
    }

    @GetMapping(path = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream() {
        return sseEventService.subscribe();
    }
}
