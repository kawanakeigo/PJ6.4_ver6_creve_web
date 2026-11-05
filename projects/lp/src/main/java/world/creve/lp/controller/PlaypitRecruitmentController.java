package world.creve.lp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import world.creve.platform.service.EventService;

/** Recruitment presentation of the same published PLAYPIT event; no copied event records. */
@Controller
public class PlaypitRecruitmentController {
    private final EventService events;
    public PlaypitRecruitmentController(EventService events) { this.events=events; }
    @GetMapping("/lp/playpit/{eventSlug}")
    public String show(@PathVariable String eventSlug, Model model) {
        model.addAttribute("event",events.getPublishedPlaypitEventBySlug(eventSlug));
        return "lp/playpit-recruitment";
    }
}
