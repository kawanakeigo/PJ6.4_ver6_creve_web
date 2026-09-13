package world.creve.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.LandingPage;
import world.creve.entity.PublishStatus;

public interface LandingPageRepository extends JpaRepository<LandingPage, String> {
  Optional<LandingPage> findBySlugAndStatus(String slug, PublishStatus status);
}
