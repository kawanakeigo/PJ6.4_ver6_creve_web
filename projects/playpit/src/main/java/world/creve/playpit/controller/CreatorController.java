package world.creve.playpit.controller;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import world.creve.platform.dto.EventResponse;
import world.creve.platform.exception.InvalidParticipationException;
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
    @GetMapping("/creators")public String showGlobalCreatorList(@RequestParam(defaultValue="0")int page,Model model) {
        model.addAttribute("creators",creators.getPublishedCreators(page));
        return "playpit/global-creator-list";
    }
    @GetMapping("/creators/{creatorSlug}")public String showGlobalCreatorDetail(@PathVariable String creatorSlug,@RequestParam(name="event",required=false)String selectedEventSlug,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="0")int artworkPage,Model model) {
        var creator=creators.getCreatorDetailBySlug(creatorSlug);
        List<EventResponse> participationEvents=events.getPublishedEventsByCreatorId(creator.creatorId());
        List<EventResponse> playpitEvents=participationEvents.stream().filter(e->"PLAYPIT".equals(e.eventType())).toList();
        EventResponse selectedEvent=selectEvent(playpitEvents,selectedEventSlug);
        model.addAttribute("creator",creator);
        model.addAttribute("creatorId",creator.creatorId());
        model.addAttribute("artworkId",null);
        model.addAttribute("events",participationEvents);
        model.addAttribute("playpitEvents",playpitEvents);
        model.addAttribute("selectedEvent",selectedEvent);
        model.addAttribute("selectedEventSlug",selectedEvent==null?null:selectedEvent.slug());
        model.addAttribute("artworks",artworks.getPublishedArtworksByCreatorId(creator.creatorId(),artworkPage));
        model.addAttribute("artworkPage",Math.max(0,artworkPage));
        if(selectedEvent!=null) {
            model.addAttribute("event",selectedEvent);
            var data=uka.getUkaData(selectedEvent.eventId(),creator.creatorId(),null);
            model.addAttribute("uka",data);
            model.addAttribute("messageCount",data.messageCount());
            var words=messages.getPublishedMessages(selectedEvent.eventId(),creator.creatorId(),null,page,30);
            model.addAttribute("messagePage",words);
            model.addAttribute("words",words.getContent().stream().map(uka::toPetal).toList());
        }
        return "playpit/global-creator-detail";
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
        model.addAttribute("selectedEventSlug",null);
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
    private EventResponse selectEvent(List<EventResponse> playpitEvents,String selectedEventSlug) {
        if(selectedEventSlug==null||selectedEventSlug.isBlank())return playpitEvents.isEmpty()?null:playpitEvents.get(0);
        return playpitEvents.stream().filter(e->selectedEventSlug.equals(e.slug())).findFirst().orElseThrow(InvalidParticipationException::new);
    }
}
