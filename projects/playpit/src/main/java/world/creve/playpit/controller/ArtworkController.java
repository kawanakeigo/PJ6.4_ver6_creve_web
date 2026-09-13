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
    private final UkaService uka;
    public ArtworkController(EventService events,ArtworkService artworks,CreatorService creators,MessageService messages,UkaService uka) {
        this.events=events;
        this.artworks=artworks;
        this.creators=creators;
        this.messages=messages;
        this.uka=uka;
    }
    @GetMapping("/playpit/{eventSlug}/artworks")public String showArtworkList(@PathVariable String eventSlug,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        model.addAttribute("event",event);
        model.addAttribute("artworks",artworks.getArtworksByEventId(event.eventId(),page));
        model.addAttribute("page",Math.max(0,page));
        return "playpit/artwork-list";
    }
    @GetMapping("/playpit/{eventSlug}/artworks/{artworkSlug}")public String showArtworkDetail(@PathVariable String eventSlug,@PathVariable String artworkSlug,@RequestParam(defaultValue="0")int page,Model model) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        var artwork=artworks.getArtworkDetailBySlug(artworkSlug);
        artworks.validateExhibition(event.eventId(),artwork.artworkId());
        creators.validateCreatorParticipation(event.eventId(),artwork.creatorId());
        model.addAttribute("event",event);
        model.addAttribute("artwork",artwork);
        model.addAttribute("creatorId",artwork.creatorId());
        model.addAttribute("artworkId",artwork.artworkId());
        var data=uka.getUkaData(event.eventId(),null,artwork.artworkId());
        model.addAttribute("uka",data);
        model.addAttribute("messageCount",data.messageCount());
        var words=messages.getPublishedMessages(event.eventId(),null,artwork.artworkId(),page,30);
        model.addAttribute("messagePage",words);
        model.addAttribute("words",words.getContent().stream().map(uka::toPetal).toList());
        return "playpit/artwork-detail";
    }
}
