package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.Document;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByClaimId(Long claimId);

    List<Document> findByContributionId(Long contributionId);
}
