package com.civicbuild.repository;

import com.civicbuild.entity.Document;
import com.civicbuild.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByApplicationId(Long applicationId);
    Optional<Document> findByApplicationIdAndDocumentType(Long applicationId, DocumentType documentType);
}
