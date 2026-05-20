package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;

import java.time.YearMonth;
import java.util.List;

public interface ContributionRepository extends JpaRepository<Contribution, Long> {

    List<Contribution> findByMemberId(Long memberId);

    List<Contribution> findAllByOrderByPaymentDateDesc();

    boolean existsByMemberIdAndContributionMonth(Long memberId, YearMonth currentMonth);

    List<Contribution> findByContributionMonthAndStatus(YearMonth month, ContributionStatus status);

    Page<Contribution> findAllByOrderByPaymentDateDesc(Pageable pageable);
}
