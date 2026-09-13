package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import world.creve.service.EventService;

@Controller
public class CreveController {
  private final EventService eventService;

  public CreveController(EventService eventService) {
    this.eventService = eventService;
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("playpitEvents", eventService.getPublishedPlaypitEvents());
    model.addAttribute("liveEvents", eventService.getPublishedLiveEvents());
    model.addAttribute("news", eventService.getLatestNews());
    model.addAttribute("active", "home");
    return "index";
  }

  @GetMapping("/about")
  public String about(Model model) {
    model.addAttribute("active", "about");
    return "about";
  }

  @GetMapping("/news")
  public String news(Model model) {
    model.addAttribute("news", eventService.getPublishedNews());
    model.addAttribute("active", "news");
    return "news/index";
  }

  @GetMapping("/vr")
  public String vr(Model model) {
    model.addAttribute("active", "vr");
    return "vr";
  }

  @GetMapping("/contact")
  public String contact(Model model) {
    model.addAttribute("active", "contact");
    return "contact/index";
  }

  @GetMapping("/privacy")
  public String privacy(Model model) {
    model.addAttribute("active", "privacy");
    return "privacy";
  }

  @GetMapping("/terms")
  public String terms(Model model) {
    model.addAttribute("active", "terms");
    return "terms";
  }
}
