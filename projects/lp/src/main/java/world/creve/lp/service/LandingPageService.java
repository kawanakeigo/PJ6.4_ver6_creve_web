package world.creve.lp.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.creve.lp.repository.LandingPageRepository;
import world.creve.lp.dto.LandingPageResponse;
import world.creve.platform.exception.EventNotFoundException;
import world.creve.platform.util.SafeUrls;
@Service @Transactional(readOnly=true) public class LandingPageService {
    private final LandingPageRepository pages;
    public LandingPageService(LandingPageRepository pages) {
        this.pages=pages;
    }
    public LandingPageResponse getPublished(String slug) {
        var p=pages.findBySlugAndStatus(slug,"PUBLISHED").orElseThrow(EventNotFoundException::new);
        return new LandingPageResponse(p.getSlug(),p.getTitle(),p.getBody(),p.getCtaLabel(),SafeUrls.optional(p.getCtaUrl()));
    }
}
