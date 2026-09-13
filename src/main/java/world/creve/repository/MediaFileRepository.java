package world.creve.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.MediaFile;

public interface MediaFileRepository extends JpaRepository<MediaFile, Long> {
  List<MediaFile> findByOwnerTypeAndOwnerIdOrderByDisplayOrderAsc(String ownerType, String ownerId);
}
