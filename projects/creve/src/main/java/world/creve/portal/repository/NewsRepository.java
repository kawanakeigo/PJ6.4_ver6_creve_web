package world.creve.portal.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.*;
import world.creve.portal.entity.News;
public interface NewsRepository extends JpaRepository<News,Long> {
    Page<News> findByStatusOrderByPublishedAtDesc(String status,Pageable pageable);
}
