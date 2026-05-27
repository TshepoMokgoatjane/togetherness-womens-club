package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;

import java.time.YearMonth;
import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    List<Contribution> findByMemberId(Long memberId);

    boolean existsByMemberIdAndContributionMonth(Long memberId, YearMonth currentMonth);

    List<Contribution> findByContributionMonthAndStatus(YearMonth month, ContributionStatus status);

    Page<Contribution> findAllByOrderByPaymentDateDesc(Pageable pageable);

    @Query("SELECT c FROM Contribution c WHERE " +
           "(LOWER(c.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.memberNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.reference) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Contribution> searchContributions(@Param("search") String search, Pageable pageable);

    Page<Contribution> findByStatus(ContributionStatus status, Pageable pageable);

    @Query("SELECT c FROM Contribution c WHERE " +
           "(LOWER(c.member.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.member.memberNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.reference) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND c.status = :status")
    Page<Contribution> searchContributionsWithStatus(@Param("search") String search, @Param("status") ContributionStatus status, Pageable pageable);

    boolean existsByMemberIdAndContributionMonthAndStatus(Long memberId, YearMonth contributionMonth, ContributionStatus contributionStatus);

    @Query("SELECT COUNT(c) FROM Contributions c WHERE c.member.id = :memberId AND c.status = :status AND c.contributionMonth IN :months")
    long countPaidMonths(@Param("memberId") Long id, @Param("status") ContributionStatus contributionStatus, @Param("months") List<YearMonth> last3Months);
}
