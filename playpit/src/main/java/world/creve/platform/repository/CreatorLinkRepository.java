package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface CreatorLinkRepository extends JpaRepository<CreatorLink,Long> {
    List<CreatorLink> findByCreatorIdOrderByDisplayOrderAsc(Long creatorId);
    @Modifying @Query("delete from CreatorLink l where l.creatorId=:id") void deleteLinks(@Param("id") Long creatorId);
}
