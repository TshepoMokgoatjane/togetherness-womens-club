package za.co.twc.togetherness.womens.club.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;

import java.util.List;

public interface BurialClaimRepository extends JpaRepository<BurialClaim, Long> {

    long countByStatus(ClaimStatus status);

    List<BurialClaim> findByMemberId(Long memberId);

    Page<BurialClaim> findAll(@NonNull Pageable pageable);
}
