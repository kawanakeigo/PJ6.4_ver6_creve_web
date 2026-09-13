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
public class CreatorController {
  private final EventService eventService;
  private final CreatorService creatorService;
  private final ArtworkService artworkService;
  private final MessageService messageService;

  public CreatorController(
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

  @GetMapping("/creators")
  public String globalCreators(Model model) {
    var event = eventService.findDefaultPlaypitEvent();
    model.addAttribute("event", event);
    model.addAttribute("eventId", event.getSlug());
    model.addAttribute("creators", creatorService.getCreatorsByEventId(event.getId()));
    model.addAttribute("active", "creators");
    return "playpit/creator-list";
  }

  @GetMapping("/creators/{creatorId}")
  public String globalCreatorDetail(@PathVariable String creatorId, Model model) {
    var event = eventService.findDefaultPlaypitEvent();
    return creatorDetail(event.getSlug(), creatorId, model);
  }

  @GetMapping("/playpit/{eventId}/creators")
  public String creators(@PathVariable String eventId, Model model) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    model.addAttribute("event", event);
    model.addAttribute("eventId", event.getSlug());
    model.addAttribute("creators", creatorService.getCreatorsByEventId(event.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/creator-list";
  }

  @GetMapping("/playpit/{eventId}/creators/{creatorId}")
  public String creatorDetail(
      @PathVariable String eventId,
      @PathVariable String creatorId,
      Model model
  ) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    var creator = creatorService.getCreatorForEvent(event.getId(), creatorId);
    model.addAttribute("event", event);
    model.addAttribute("creator", creator);
    model.addAttribute("artworks", artworkService.getArtworksByEventIdAndCreatorId(
        event.getId(),
        creator.getId()
    ));
    model.addAttribute("messages", messageService.findPublicByCreator(event.getId(), creator.getId()));
    model.addAttribute("messageCount", messageService.countPublicByCreator(event.getId(), creator.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/creator-detail";
  }
}
