package world.creve.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import world.creve.service.LandingPageService;

@Controller
public class LandingPageController {
  private final LandingPageService landingPageService;

  public LandingPageController(LandingPageService landingPageService) {
    this.landingPageService = landingPageService;
  }

  @GetMapping("/lp/{lpId}")
  public String landingPage(@PathVariable String lpId, Model model) {
    model.addAttribute("landingPage", landingPageService.findPublishedLandingPage(lpId));
    model.addAttribute("active", "lp");
    return "lp/detail";
  }
}
