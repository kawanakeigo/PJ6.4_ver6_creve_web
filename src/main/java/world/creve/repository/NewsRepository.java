package world.creve.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.News;
import world.creve.entity.PublishStatus;

public interface NewsRepository extends JpaRepository<News, Long> {
  Optional<News> findBySlugAndStatus(String slug, PublishStatus status);

  List<News> findTop3ByStatusOrderByPublishedAtDesc(PublishStatus status);

  List<News> findByStatusOrderByPublishedAtDesc(PublishStatus status);
}
