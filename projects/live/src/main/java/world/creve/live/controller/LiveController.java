package world.creve.live.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.live.service.LiveService;
import world.creve.platform.service.CreatorService;
@Controller public class LiveController {
    private final LiveService live;
    private final CreatorService creators;
    public LiveController(LiveService live,CreatorService creators) {
        this.live=live;
        this.creators=creators;
    }
    @GetMapping("/live")public String showLiveList(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("events",live.list(page));
        return "live/list";
    }
    @GetMapping("/live/{eventSlug}")public String showLiveDetail(@PathVariable String eventSlug,Model model) {
        var event=live.detail(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("creators",creators.getCreatorsByEventId(event.eventId()));
        return "live/detail";
    }
}
