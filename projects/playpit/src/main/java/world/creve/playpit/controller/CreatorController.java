package world.creve.playpit.controller;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.util.UriComponentsBuilder;
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
    public CreatorController(EventService events,CreatorService creators,ArtworkService artworks,MessageService messages) {
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.messages=messages;
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
        var displayedWorks=selectedEvent==null ? artworks.getPublishedArtworksByCreatorId(creator.creatorId(),artworkPage) : artworks.getArtworksByEventAndCreator(selectedEvent.eventId(),creator.creatorId(),artworkPage);
        // Keep the opening selection stable while paging through the same public/event scope.
        var firstWorks=artworkPage<=0 ? displayedWorks : (selectedEvent==null
            ? artworks.getPublishedArtworksByCreatorId(creator.creatorId(),0)
            : artworks.getArtworksByEventAndCreator(selectedEvent.eventId(),creator.creatorId(),0));
        model.addAttribute("artworks",displayedWorks);
        model.addAttribute("openingWorks",firstWorks.stream().limit(3).toList());
        model.addAttribute("artworkPage",Math.max(0,artworkPage));
        if(selectedEvent!=null) {
            model.addAttribute("event",selectedEvent);
            var words=messages.getPublishedMessages(selectedEvent.eventId(),creator.creatorId(),null,page,30);
            model.addAttribute("messagePage",words);
            model.addAttribute("words",words.getContent().stream().map(world.creve.playpit.dto.response.WordResponse::from).toList());
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
    @GetMapping("/playpit/{eventSlug}/creators/{creatorSlug}")public ResponseEntity<Void> showCreatorDetail(@PathVariable String eventSlug,@PathVariable String creatorSlug,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="0")int artworkPage,@RequestParam(required=false)String source) {
        var event=events.getPublishedPlaypitEventBySlug(eventSlug);
        var creator=creators.getCreatorDetailBySlug(creatorSlug);
        creators.validateCreatorParticipation(event.eventId(),creator.creatorId());
        var destination=UriComponentsBuilder.fromPath("/creators/{creator}")
            .queryParam("event",event.slug()).queryParam("page",Math.max(0,page))
            .queryParam("artworkPage",Math.max(0,artworkPage));
        if("qr".equals(source))destination.queryParam("source","qr");
        return ResponseEntity.status(HttpStatus.PERMANENT_REDIRECT)
            .location(destination.buildAndExpand(creator.slug()).encode().toUri()).build();
    }
    private EventResponse selectEvent(List<EventResponse> playpitEvents,String selectedEventSlug) {
        if(selectedEventSlug==null||selectedEventSlug.isBlank())return playpitEvents.isEmpty()?null:playpitEvents.get(0);
        return playpitEvents.stream().filter(e->selectedEventSlug.equals(e.slug())).findFirst().orElseThrow(InvalidParticipationException::new);
    }
}
