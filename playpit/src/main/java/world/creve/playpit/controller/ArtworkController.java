package world.creve.playpit.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.service.*;
import world.creve.playpit.service.*;
@Controller public class ArtworkController {
    private final EventService events;
    private final ArtworkService artworks;
    private final CreatorService creators;
    private final MessageService messages;
    public ArtworkController(EventService events,ArtworkService artworks,CreatorService creators,MessageService messages) {
        this.events=events;
        this.artworks=artworks;
        this.creators=creators;
        this.messages=messages;
    }
    @GetMapping("/playpit/{eventSlug}/artworks")public String showArtworkList(@PathVariable String eventSlug,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("artworks",artworks.getArtworksByEventId(event.eventId(),page));
        model.addAttribute("page",Math.max(0,page));
        return "playpit/artwork-list";
    }
    @GetMapping("/playpit/{eventSlug}/artworks/{artworkId}")public String showArtworkDetail(@PathVariable String eventSlug,@PathVariable Long artworkId,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        var artwork=artworks.getArtworkDetailById(artworkId);
        artworks.validateExhibition(event.eventId(),artwork.artworkId());
        creators.validateCreatorParticipation(event.eventId(),artwork.creatorId());
        model.addAttribute("event",event);
        model.addAttribute("artwork",artwork);
        model.addAttribute("creatorId",artwork.creatorId());
        model.addAttribute("artworkId",artwork.artworkId());
        model.addAttribute("selectedEventSlug",null);
        var words=messages.getPublishedMessages(event.eventId(),null,artwork.artworkId(),page,30);
        model.addAttribute("messageCount",words.getTotalElements());
        model.addAttribute("messagePage",words);
        model.addAttribute("words",words.getContent());
        return "playpit/artwork-detail";
    }
}
