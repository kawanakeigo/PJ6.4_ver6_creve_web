package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.*;
import world.creve.playpit.service.*;
@Controller public class PlaypitController {
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final MessageService messages;
    public PlaypitController(EventService events,CreatorService creators,ArtworkService artworks,MessageService messages) {
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.messages=messages;
    }
    @GetMapping("/playpit")public String showTopPage(Model model) {
        model.addAttribute("events",events.list("PLAYPIT",0));
        return "playpit/index";
    }
    @GetMapping("/playpit/events")public String showEventList(@RequestParam(defaultValue="0")int page,Model model) {
        var data=events.list("PLAYPIT",page);
        model.addAttribute("events",data);
        model.addAttribute("eventGroups",group(data.getContent()));
        model.addAttribute("archive",false);
        return "playpit/event-list";
    }
    @GetMapping("/playpit/archive")public String showArchive(@RequestParam(defaultValue="0")int page,Model model) {
        var data=events.archive("PLAYPIT",page);
        model.addAttribute("events",data);
        model.addAttribute("eventGroups",group(data.getContent()));
        model.addAttribute("archive",true);
        return "playpit/event-list";
    }
    @GetMapping("/playpit/{eventSlug}")public String showEventDetail(@PathVariable String eventSlug,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("creators",creators.getCreatorsByEventId(event.eventId()));
        model.addAttribute("artworks",artworks.getArtworksByEventId(event.eventId()));
        model.addAttribute("messageCount",messages.countPublishedMessagesByEventId(event.eventId()));
        return "playpit/event-detail";
    }
    private java.util.Map<String,java.util.List<world.creve.platform.dto.EventResponse>> group(java.util.List<world.creve.platform.dto.EventResponse> data) {
        var groups=new java.util.LinkedHashMap<String,java.util.List<world.creve.platform.dto.EventResponse>>();
        for(String phase:java.util.List.of("開催予定","開催中","終了済み"))groups.put(phase,data.stream().filter(e->phase.equals(e.phase())).toList());
        return groups;
    }
}
