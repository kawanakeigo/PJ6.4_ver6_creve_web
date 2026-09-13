package world.creve.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import world.creve.exception.BadRequestException;
import world.creve.dto.request.MessageRequest;
import world.creve.dto.response.MessageResponse;
import world.creve.dto.response.UkaResponse;
import world.creve.service.MessageService;
import world.creve.service.UkaService;

@RestController
@RequestMapping("/api")
public class MessageController {
  private final MessageService messageService;
  private final UkaService ukaService;

  public MessageController(MessageService messageService, UkaService ukaService) {
    this.messageService = messageService;
    this.ukaService = ukaService;
  }

  @PostMapping("/messages")
  @ResponseStatus(HttpStatus.CREATED)
  public MessageResponse create(
      @Valid @RequestBody MessageRequest request,
      HttpServletRequest servletRequest
  ) {
    return messageService.registerMessage(request, servletRequest);
  }

  @GetMapping("/messages")
  public List<MessageResponse> messages(
      @RequestParam(required = false) String eventId,
      @RequestParam(required = false) String creatorId,
      @RequestParam(required = false) String artworkId
  ) {
    if (eventId == null || eventId.trim().isEmpty()) {
      throw new BadRequestException("eventIdを指定してください。");
    }
    if (artworkId != null && !artworkId.trim().isEmpty()) {
      return messageService.findPublicByArtwork(eventId, artworkId);
    }
    if (creatorId != null && !creatorId.trim().isEmpty()) {
      return messageService.findPublicByCreator(eventId, creatorId);
    }
    return messageService.findPublicByEvent(eventId);
  }

  @GetMapping("/playpit/{eventId}/uka")
  public UkaResponse eventUka(@PathVariable String eventId) {
    return ukaService.eventUka(eventId);
  }
}
