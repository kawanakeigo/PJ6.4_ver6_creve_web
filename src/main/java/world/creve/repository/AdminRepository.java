package world.creve.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import world.creve.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
  Optional<Admin> findByEmail(String email);
}
