package com.example.backend.service.stream;

import jakarta.annotation.PreDestroy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SseEventService {

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        emitters.add(emitter);

        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(error -> emitters.remove(emitter));

        return emitter;
    }

    @Async
    public void publish(String eventName, String eventId, Object data) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .id(eventId)
                        .data(data));
            } catch (IOException | IllegalStateException exception) {
                remove(emitter, exception);
            }
        }
    }

    @Scheduled(fixedRateString = "${app.sse.heartbeat-ms:15000}")
    public void sendHeartbeat() {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().comment("heartbeat"));
            } catch (IOException | IllegalStateException exception) {
                remove(emitter, exception);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        emitters.forEach(SseEmitter::complete);
        emitters.clear();
    }

    private void remove(SseEmitter emitter, Exception exception) {
        emitters.remove(emitter);
        emitter.completeWithError(exception);
    }
}
