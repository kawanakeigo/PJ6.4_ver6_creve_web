package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import world.creve.service.LiveService;

@Controller
public class LiveController {
  private final LiveService liveService;

  public LiveController(LiveService liveService) {
    this.liveService = liveService;
  }

  @GetMapping("/live")
  public String live(Model model) {
    model.addAttribute("events", liveService.getPublishedLiveEvents());
    model.addAttribute("active", "live");
    return "live/list";
  }

  @GetMapping("/live/{eventId}")
  public String liveDetail(@PathVariable String eventId, Model model) {
    model.addAttribute("event", liveService.getPublishedLiveEvent(eventId));
    model.addAttribute("active", "live");
    return "live/detail";
  }
}
