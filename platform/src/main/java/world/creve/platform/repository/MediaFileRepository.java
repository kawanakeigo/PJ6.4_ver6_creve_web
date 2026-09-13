package world.creve.platform.repository;
import java.util.*;
import java.time.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.domain.*;
import org.springframework.data.repository.query.Param;
import world.creve.platform.entity.*;
import world.creve.platform.dto.*;
public interface MediaFileRepository extends JpaRepository<MediaFile,Long> {
    List<MediaFile> findByArtworkIdOrderByDisplayOrderAsc(Long artworkId);
    @Modifying @Query("delete from MediaFile m where m.artworkId=:id") void deleteMedia(@Param("id") Long artworkId);
}
