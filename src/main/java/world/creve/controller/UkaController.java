package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import world.creve.service.CreatorService;
import world.creve.service.EventService;
import world.creve.service.MessageService;

@Controller
public class UkaController {
  private final EventService eventService;
  private final CreatorService creatorService;
  private final MessageService messageService;

  public UkaController(
      EventService eventService,
      CreatorService creatorService,
      MessageService messageService
  ) {
    this.eventService = eventService;
    this.creatorService = creatorService;
    this.messageService = messageService;
  }

  @GetMapping("/playpit/{eventId}/uka")
  public String uka(@PathVariable String eventId, Model model) {
    var event = eventService.findPublishedPlaypitEventBySlug(eventId);
    model.addAttribute("event", event);
    model.addAttribute("messages", messageService.findPublicByEvent(event.getId()));
    model.addAttribute("messageCount", messageService.countPublicByEvent(event.getId()));
    model.addAttribute("creators", creatorService.getCreatorsByEventId(event.getId()));
    model.addAttribute("active", "playpit");
    return "playpit/uka";
  }
}
