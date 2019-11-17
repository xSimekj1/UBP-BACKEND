package team.project.upb.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import team.project.upb.api.model.FileMetadata;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {
}
