package team.project.upb.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import team.project.upb.api.model.FileMetadata;

import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    @Query("SELECT fm FROM FileMetadata fm WHERE fm.receiver.id = (:id)")
    List<FileMetadata> findAllByReceiverId(@Param("id") Long id);
}
