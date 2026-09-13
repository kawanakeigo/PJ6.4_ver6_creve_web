package world.creve.lp.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.lp.service.LandingPageService;
@Controller public class LandingPageController {
    private final LandingPageService pages;
    public LandingPageController(LandingPageService pages) {
        this.pages=pages;
    }
    @GetMapping("/lp/{lpId}")public String showLandingPage(@PathVariable String lpId,Model model) {
        model.addAttribute("landingPage",pages.getPublished(lpId));
        return "lp/detail";
    }
}
