package world.creve.playpit.service;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.*;
import world.creve.platform.entity.*;
import world.creve.platform.repository.*;
import world.creve.platform.exception.*;
import world.creve.platform.util.SafeUrls;
import world.creve.playpit.dto.request.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Service @Transactional public class ContentAdminService {
    private static final Logger log=LoggerFactory.getLogger(ContentAdminService.class);
    private final EventRepository events;
    private final CreatorRepository creators;
    private final ArtworkRepository artworks;
    private final EventCreatorRepository participants;
    private final EventArtworkRepository exhibitions;
    private final CreatorLinkRepository links;
    private final MediaFileRepository media;
    public ContentAdminService(EventRepository events,CreatorRepository creators,ArtworkRepository artworks,EventCreatorRepository participants,EventArtworkRepository exhibitions,CreatorLinkRepository links,MediaFileRepository media) {
        this.events=events;
        this.creators=creators;
        this.artworks=artworks;
        this.participants=participants;
        this.exhibitions=exhibitions;
        this.links=links;
        this.media=media;
    }
    private static String url(String v) {
        try {
            return SafeUrls.optional(v);
        }
        catch(IllegalArgumentException ex) {
            throw new InvalidMessageException("URLを確認してください。");
        }
    }
    private static String slug(String v) {
        try {
            return SafeUrls.slug(v);
        }
        catch(IllegalArgumentException ex) {
            throw new InvalidMessageException("識別子を確認してください。");
        }
    }
    public Event event(Long id) {
        return events.findById(id).orElseThrow(EventNotFoundException::new);
    }
    public Creator creator(Long id) {
        return creators.findById(id).orElseThrow(CreatorNotFoundException::new);
    }
    public Artwork artwork(Long id) {
        return artworks.findById(id).orElseThrow(ArtworkNotFoundException::new);
    }
    public Page<Event> events(int page) {
        return events.findAll(PageRequest.of(Math.max(0,page),30,Sort.by("eventId").descending()));
    }
    public Page<Creator> creators(int page) {
        return creators.findAll(PageRequest.of(Math.max(0,page),30,Sort.by("creatorId")));
    }
    public Page<Artwork> artworks(int page) {
        return artworks.findAll(PageRequest.of(Math.max(0,page),30,Sort.by("artworkId")));
    }
    public List<EventCreator> participants(Long eventId) {
        return participants.findByEventIdOrderByDisplayOrderAsc(eventId,PageRequest.of(0,300)).getContent();
    }
    public List<EventArtwork> exhibitions(Long eventId) {
        return exhibitions.findByEventIdOrderByDisplayOrderAsc(eventId,PageRequest.of(0,300)).getContent();
    }
    public Long saveEvent(Long id,EventForm f) {
        if(f.getStartAt()==null||f.getEndAt()==null||f.getEndAt().isBefore(f.getStartAt()))throw new InvalidMessageException("開催日時を確認してください。");
        if(id==null?events.existsBySlug(f.getSlug()):events.existsBySlugAndEventIdNot(f.getSlug(),id))throw new InvalidMessageException("この識別子は使用中です。");
        Event e=id==null?new Event():event(id);
        e.setEventType(f.getEventType());
        e.setSlug(slug(f.getSlug()));
        e.setTitle(f.getTitle());
        e.setSummary(f.getSummary());
        e.setDescription(f.getDescription());
        e.setMainImageUrl(url(f.getMainImageUrl()));
        e.setStartAt(f.getStartAt());
        e.setEndAt(f.getEndAt());
        e.setVenueName(f.getVenueName());
        e.setAddress(f.getAddress());
        e.setAccess(f.getAccess());
        e.setPrice(f.getPrice());
        e.setTicketUrl(url(f.getTicketUrl()));
        e.setStatus(f.getStatus());
        events.save(e);
        log.info("event_saved eventId={}",e.getEventId());
        return e.getEventId();
    }
    public Long saveCreator(Long id,CreatorForm f) {
        if(id==null?creators.existsBySlug(f.getSlug()):creators.existsBySlugAndCreatorIdNot(f.getSlug(),id))throw new InvalidMessageException("この識別子は使用中です。");
        Creator c=id==null?new Creator():creator(id);
        c.setSlug(slug(f.getSlug()));
        c.setName(f.getName());
        c.setNameKana(f.getNameKana());
        c.setProfile(f.getProfile());
        c.setConcept(f.getConcept());
        c.setGenre(f.getGenre());
        c.setProfileImageUrl(url(f.getProfileImageUrl()));
        c.setWebsiteUrl(url(f.getWebsiteUrl()));
        c.setStatus(f.getStatus());
        creators.saveAndFlush(c);
        links.deleteLinks(c.getCreatorId());
        int order=0;
        if(f.getSnsLinks()!=null)for(String line:f.getSnsLinks().split("\\R")) {
            if(line.isBlank())continue;
            String[] parts=line.split("\\|",2);
            if(parts.length!=2||parts[0].length()>80)throw new InvalidMessageException("SNSは ラベル|URL の形式で入力してください。");
            CreatorLink l=new CreatorLink();
            l.setCreatorId(c.getCreatorId());
            l.setLabel(parts[0].trim());
            l.setUrl(url(parts[1]));
            if(l.getUrl()==null)throw new InvalidMessageException();
            l.setDisplayOrder(order++);
            links.save(l);
        }
        log.info("creator_saved creatorId={}",c.getCreatorId());
        return c.getCreatorId();
    }
    public Long saveArtwork(Long id,ArtworkForm f) {
        creator(f.getCreatorId());
        if(id==null?artworks.existsBySlug(f.getSlug()):artworks.existsBySlugAndArtworkIdNot(f.getSlug(),id))throw new InvalidMessageException("この識別子は使用中です。");
        Artwork a=id==null?new Artwork():artwork(id);
        if(id!=null&&!a.getCreatorId().equals(f.getCreatorId()))throw new InvalidMessageException("登録後の制作者変更は、参加関係・既存感想に影響するためこの画面では変更できません。");
        a.setCreatorId(f.getCreatorId());
        a.setSlug(slug(f.getSlug()));
        a.setTitle(f.getTitle());
        a.setDescription(f.getDescription());
        a.setBackground(f.getBackground());
        a.setConcept(f.getConcept());
        a.setMaterials(f.getMaterials());
        a.setProductionYear(f.getProductionYear());
        a.setMainImageUrl(url(f.getMainImageUrl()));
        a.setStatus(f.getStatus());
        artworks.saveAndFlush(a);
        media.deleteMedia(a.getArtworkId());
        int order=0;
        if(f.getMediaLines()!=null)for(String line:f.getMediaLines().split("\\R")) {
            if(line.isBlank())continue;
            String[] parts=line.split("\\|",3);
            if(parts.length<2||!Set.of("IMAGE","VIDEO","AUDIO").contains(parts[0]))throw new InvalidMessageException("メディアは IMAGE/VIDEO/AUDIO|URL|説明 の形式で入力してください。");
            MediaFile m=new MediaFile();
            m.setArtworkId(a.getArtworkId());
            m.setMediaType(parts[0]);
            m.setUrl(url(parts[1]));
            if(m.getUrl()==null)throw new InvalidMessageException();
            m.setAltText(parts.length==3?parts[2]:"");
            if(m.getAltText().length()>300)throw new InvalidMessageException();
            m.setDisplayOrder(order++);
            media.save(m);
        }
        log.info("artwork_saved artworkId={}",a.getArtworkId());
        return a.getArtworkId();
    }
    public void addParticipant(Long eventId,Long creatorId,int order,String title) {
        event(eventId);
        creator(creatorId);
        if(title!=null&&title.length()>150)throw new InvalidMessageException();
        var ec=participants.findByEventIdAndCreatorId(eventId,creatorId).orElseGet(EventCreator::new);
        ec.setEventId(eventId);
        ec.setCreatorId(creatorId);
        ec.setDisplayOrder(order);
        ec.setExhibitionTitle(title);
        participants.save(ec);
    }
    public void addExhibition(Long eventId,Long artworkId,int order,String area) {
        Event e=event(eventId);
        Artwork a=artwork(artworkId);
        if(!participants.existsByEventIdAndCreatorId(eventId,a.getCreatorId()))throw new InvalidParticipationException();
        if(area!=null&&area.length()>150)throw new InvalidMessageException();
        var ea=exhibitions.findByEventIdAndArtworkId(eventId,artworkId).orElseGet(EventArtwork::new);
        ea.setEventId(eventId);
        ea.setArtworkId(artworkId);
        ea.setDisplayOrder(order);
        ea.setExhibitionArea(area);
        ea.setQrCodeUrl("/playpit/"+e.getSlug()+"/artworks/"+a.getSlug()+"/qr.png");
        exhibitions.save(ea);
    }
    public String snsLines(Long id) {
        return String.join("\n",links.findByCreatorIdOrderByDisplayOrderAsc(id).stream().map(l->l.getLabel()+"|"+l.getUrl()).toList());
    }
    public String mediaLines(Long id) {
        return String.join("\n",media.findByArtworkIdOrderByDisplayOrderAsc(id).stream().map(m->m.getMediaType()+"|"+m.getUrl()+"|"+(m.getAltText()==null?"":m.getAltText())).toList());
    }
}
