package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.*;
import world.creve.playpit.service.*;
@Controller public class CreatorController {
    private final EventService events;
    private final CreatorService creators;
    private final ArtworkService artworks;
    private final MessageService messages;
    private final UkaService uka;
    public CreatorController(EventService events,CreatorService creators,ArtworkService artworks,MessageService messages,UkaService uka) {
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.messages=messages;
        this.uka=uka;
    }
    @GetMapping("/playpit/{eventSlug}/creators")public String showCreatorList(@PathVariable String eventSlug,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("creators",creators.getCreatorsByEventId(event.eventId(),page));
        model.addAttribute("page",Math.max(0,page));
        return "playpit/creator-list";
    }
    @GetMapping("/playpit/{eventSlug}/creators/{creatorSlug}")public String showCreatorDetail(@PathVariable String eventSlug,@PathVariable String creatorSlug,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="0")int artworkPage,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        var creator=creators.getCreatorDetailBySlug(creatorSlug);
        creators.validateCreatorParticipation(event.eventId(),creator.creatorId());
        model.addAttribute("event",event);
        model.addAttribute("creator",creator);
        model.addAttribute("creatorId",creator.creatorId());
        model.addAttribute("artworkId",null);
        model.addAttribute("artworks",artworks.getArtworksByEventAndCreator(event.eventId(),creator.creatorId(),artworkPage));
        model.addAttribute("artworkPage",Math.max(0,artworkPage));
        var data=uka.getUkaData(event.eventId(),creator.creatorId(),null);
        model.addAttribute("uka",data);
        model.addAttribute("messageCount",data.messageCount());
        var words=messages.getPublishedMessages(event.eventId(),creator.creatorId(),null,page,30);
        model.addAttribute("messagePage",words);
        model.addAttribute("words",words.getContent().stream().map(uka::toPetal).toList());
        return "playpit/creator-detail";
    }
}
