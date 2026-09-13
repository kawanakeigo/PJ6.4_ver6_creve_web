package world.creve.playpit.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import world.creve.playpit.dto.request.*;
import world.creve.playpit.dto.response.*;
import world.creve.playpit.service.*;
@RestController @RequestMapping("/api/messages") public class MessageController {
    private final MessageService messages;
    private final UkaService uka;
    public MessageController(MessageService messages,UkaService uka) {
        this.messages=messages;
        this.uka=uka;
    }
    @PostMapping public ResponseEntity<MessageResponse> createMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(201).body(messages.registerMessage(request));
    }
    @GetMapping public PageResponse getMessages(@RequestParam Long eventId,@RequestParam(required=false)Long creatorId,@RequestParam(required=false)Long artworkId,@RequestParam(defaultValue="0")int page) {
        var data=messages.getPublishedMessages(eventId,creatorId,artworkId,page,30);
        return new PageResponse(data.getContent().stream().map(uka::toPetal).toList(),data.getNumber(),data.getSize(),data.getTotalElements(),data.getTotalPages());
    }
}
