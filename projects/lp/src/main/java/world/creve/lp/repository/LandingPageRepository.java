package world.creve.lp.repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.lp.entity.LandingPage;
public interface LandingPageRepository extends JpaRepository<LandingPage,Long> {
    Optional<LandingPage> findBySlugAndStatus(String slug,String status);
}
