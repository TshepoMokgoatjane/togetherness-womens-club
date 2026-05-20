package za.co.twc.togetherness.womens.club.repository;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;

import java.util.List;

public interface BurialClaimRepository extends JpaRepository<BurialClaim, Long> {

    long countByStatus(ClaimStatus status);

    List<BurialClaim> findByMemberId(Long memberId);

    Page<BurialClaim> findAll(@NonNull Pageable pageable);

    Page<BurialClaim> findByStatus(ClaimStatus status, Pageable pageable);

    @Query("SELECT c FROM BurialClaim c WHERE " +
           "(LOWER(c.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.lastName) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<BurialClaim> searchByMemberName(@Param("search") String search, Pageable pageable);

    @Query("SELECT c FROM BurialClaim c WHERE " +
           "(LOWER(c.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.lastName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.status = :status")
    Page<BurialClaim> searchByMemberNameAndStatus(@Param("search") String search, @Param("status") ClaimStatus status, Pageable pageable);
}
