package world.creve.platform.service;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.dto.*;
import world.creve.platform.exception.*;
import world.creve.platform.util.PageBounds;
@Service @Transactional(readOnly=true) public class CreatorService {
    private final CreatorRepository creators;
    private final EventCreatorRepository participants;
    private final CreatorLinkRepository links;
    public CreatorService(CreatorRepository creators,EventCreatorRepository participants,CreatorLinkRepository links) {
        this.creators=creators;
        this.participants=participants;
        this.links=links;
    }
    public Creator requirePublished(Long id) {
        return creators.findById(id).filter(c->"PUBLISHED".equals(c.getStatus())).orElseThrow(CreatorNotFoundException::new);
    }
    public CreatorDetailResponse getCreatorDetailBySlug(String slug) {
        return detail(creators.findBySlugAndStatus(slug,"PUBLISHED").orElseThrow(CreatorNotFoundException::new));
    }
    public void validateCreatorParticipation(Long eventId,Long creatorId) {
        if(!participants.existsByEventIdAndCreatorId(eventId,creatorId))throw new InvalidParticipationException();
    }
    public List<CreatorSummaryResponse> getCreatorsByEventId(Long eventId) {
        return getCreatorsByEventId(eventId,0);
    }
    public List<CreatorSummaryResponse> getCreatorsByEventId(Long eventId,int page) {
        return creators.findCreatorsByEventId(eventId,PageRequest.of(PageBounds.page(page),30));
    }
    public CreatorDetailResponse detail(Creator c) {
        return new CreatorDetailResponse(c.getCreatorId(),c.getSlug(),c.getName(),c.getNameKana(),c.getProfile(),c.getConcept(),c.getGenre(),c.getProfileImageUrl(),c.getWebsiteUrl(),links.findByCreatorIdOrderByDisplayOrderAsc(c.getCreatorId()).stream().map(x->new CreatorLinkResponse(x.getLabel(),x.getUrl())).toList());
    }
}
