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
public class PlaypitController {
  private final EventService eventService;
  private final CreatorService creatorService;
  private final ArtworkService artworkService;
  private final MessageService messageService;

  public PlaypitController(
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

  @GetMapping("/playpit")
  public String index(Model model) {
    model.addAttribute("events", eventService.getPublishedPlaypitEvents());
    model.addAttribute("featuredEvent", eventService.findDefaultPlaypitEvent());
    model.addAttribute("active", "playpit");
    return "playpit/index";
  }

  @GetMapping("/playpit/events")
  public String events(Model model) {
    model.addAttribute("events", eventService.getPublishedPlaypitEvents());
    model.addAttribute("active", "playpit");
    return "playpit/event-list";
  }

  @GetMapping("/playpit/archive")
  public String archive(Model model) {
    model.addAttribute("events", eventService.getPublishedPlaypitEvents());
    model.addAttribute("active", "playpit");
    return "playpit/event-list";
  }

  @GetMapping("/playpit/{eventId}")
  public String eventDetail(@PathVariable String eventId, Model model) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    model.addAttribute("event", event);
    model.addAttribute("creators", creatorService.getCreatorsByEventId(event.getId()));
    model.addAttribute("artworks", artworkService.getArtworksByEventId(event.getId()));
    model.addAttribute("messageCount", messageService.countPublicByEvent(event.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/event-detail";
  }
}
