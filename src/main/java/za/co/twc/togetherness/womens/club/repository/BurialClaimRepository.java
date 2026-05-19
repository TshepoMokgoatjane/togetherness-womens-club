package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;

public interface BurialClaimRepository extends JpaRepository<BurialClaim, Long> {

    long countByStatus(ClaimStatus status);

    java.util.List<BurialClaim> findByMemberId(Long memberId);
}
