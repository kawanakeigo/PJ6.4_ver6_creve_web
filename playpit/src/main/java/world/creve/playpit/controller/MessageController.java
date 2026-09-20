package world.creve.playpit.controller;

import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import world.creve.playpit.dto.request.MessageRequest;
import world.creve.playpit.dto.response.MessageResponse;
import world.creve.playpit.dto.response.PageResponse;
import world.creve.playpit.service.MessageService;

@RestController
@RequestMapping("/api")
public class MessageController {
    private final MessageService messages;

    public MessageController(MessageService messages) {
        this.messages = messages;
    }

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> createMessage(
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("Idempotency-Key") UUID requestKey) {
        return ResponseEntity.status(201).body(messages.registerMessage(request, requestKey));
    }

    @GetMapping("/message-receipts/{key}")
    public ResponseEntity<MessageResponse> receipt(@PathVariable UUID key) {
        return messages.findReceipt(key).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping({"/public/messages", "/messages"})
    public PageResponse getMessages(
            @RequestParam Long eventId,
            @RequestParam(required = false) Long creatorId,
            @RequestParam(required = false) Long artworkId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var result = messages.getPublishedMessages(eventId, creatorId, artworkId, page, size);
        return new PageResponse(result.getContent(), result.getNumber(), result.getSize(),
                result.getTotalElements(), result.hasNext());
    }
}
