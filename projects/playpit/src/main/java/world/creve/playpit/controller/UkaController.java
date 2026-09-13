package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.EventService;
import world.creve.playpit.service.*;
import world.creve.playpit.dto.response.UkaResponse;
@Controller public class UkaController {
    private final EventService events;
    private final UkaService uka;
    private final MessageService messages;
    public UkaController(EventService events,UkaService uka,MessageService messages) {
        this.events=events;
        this.uka=uka;
        this.messages=messages;
    }
    @GetMapping("/playpit/{eventSlug}/uka") public String showUka(@PathVariable String eventSlug,@RequestParam(required=false)Long creatorId,@RequestParam(required=false)Long artworkId,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        var data=uka.getUkaData(event.eventId(),creatorId,artworkId);
        model.addAttribute("event",event);
        model.addAttribute("uka",data);
        model.addAttribute("growthStage",data.growthStage());
        model.addAttribute("messageCount",data.messageCount());
        var words=messages.getPublishedMessages(event.eventId(),creatorId,artworkId,page,30);
        model.addAttribute("messagePage",words);
        model.addAttribute("words",words.getContent().stream().map(uka::toPetal).toList());
        model.addAttribute("creatorCounts",messages.creatorCounts(event.eventId()));
        model.addAttribute("creatorId",creatorId);
        model.addAttribute("artworkId",artworkId);
        model.addAttribute("selectedEventSlug",null);
        return "playpit/uka";
    }
    @GetMapping("/api/uka") @ResponseBody public UkaResponse getUkaData(@RequestParam Long eventId,@RequestParam(required=false)Long creatorId,@RequestParam(required=false)Long artworkId) {
        return uka.getUkaData(eventId,creatorId,artworkId);
    }
}
