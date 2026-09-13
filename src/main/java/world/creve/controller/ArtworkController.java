package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import world.creve.service.ArtworkService;
import world.creve.service.CreatorService;
import world.creve.service.EventService;
import world.creve.service.MessageService;

@Controller
public class ArtworkController {
  private final EventService eventService;
  private final CreatorService creatorService;
  private final ArtworkService artworkService;
  private final MessageService messageService;

  public ArtworkController(
      EventService eventService,
      CreatorService creatorService,
      ArtworkService artworkService,
      MessageService messageService
  ) {
    this.eventService = eventService;
    this.creatorService = creatorService;
    this.artworkService = artworkService;
    this.messageService = messageService;
  }

  @GetMapping("/playpit/{eventId}/artworks")
  public String artworks(@PathVariable String eventId, Model model) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    model.addAttribute("event", event);
    model.addAttribute("artworks", artworkService.getArtworksByEventId(event.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/artwork-list";
  }

  @GetMapping("/playpit/{eventId}/artworks/{artworkId}")
  public String artworkDetail(
      @PathVariable String eventId,
      @PathVariable String artworkId,
      Model model
  ) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    var artwork = artworkService.getArtworkForEvent(event.getId(), artworkId);
    model.addAttribute("event", event);
    model.addAttribute("artwork", artwork);
    model.addAttribute("creator", creatorService.findById(artwork.getCreatorId()));
    model.addAttribute("messages", messageService.findPublicByArtwork(event.getId(), artwork.getId()));
    model.addAttribute("messageCount", messageService.countPublicByArtwork(event.getId(), artwork.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/artwork-detail";
  }
}
