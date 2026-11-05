package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.EventService;
import world.creve.playpit.service.PlaypitContactService;
import world.creve.playpit.service.PlaypitNewsService;
@Controller public class PlaypitInfoController {
    private final EventService events;
    private final PlaypitContactService contact;
    private final PlaypitNewsService news;
    public PlaypitInfoController(EventService events,PlaypitContactService contact,PlaypitNewsService news) {this.events=events;this.contact=contact;this.news=news;}
    @GetMapping("/playpit/about") public String about(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("pastEvents",events.archive("PLAYPIT",page));
        return "playpit/about";
    }
    @GetMapping("/playpit/news") public String news(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("events",events.list("PLAYPIT",page));
        model.addAttribute("notices",news.notices());
        return "playpit/news";
    }
    @GetMapping("/playpit/contact") public String contact(Model model) {
        model.addAttribute("contactContent",contact.content());
        model.addAttribute("contactFormUrl",contact.formUrl());
        return "playpit/contact";
    }
}
