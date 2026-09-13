package world.creve.service;

import org.springframework.stereotype.Service;
import world.creve.entity.LandingPage;
import world.creve.entity.PublishStatus;
import world.creve.exception.NotFoundException;
import world.creve.repository.LandingPageRepository;

@Service
public class LandingPageService {
  private final LandingPageRepository landingPageRepository;

  public LandingPageService(LandingPageRepository landingPageRepository) {
    this.landingPageRepository = landingPageRepository;
  }

  public LandingPage findPublishedLandingPage(String lpSlug) {
    return landingPageRepository.findBySlugAndStatus(lpSlug, PublishStatus.PUBLISHED)
        .or(() -> landingPageRepository.findById(lpSlug)
            .filter(landingPage -> landingPage.getStatus() == PublishStatus.PUBLISHED))
        .orElseThrow(() -> new NotFoundException("LPが見つかりません。"));
  }
}
