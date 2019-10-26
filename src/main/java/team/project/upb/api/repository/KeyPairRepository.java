package team.project.upb.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.project.upb.api.model.KeyPairPP;

@Repository
public interface KeyPairRepository extends JpaRepository<KeyPairPP, Long> {
}
