package za.co.twc.togetherness.womens.club.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.DuplicateMonthlyContributionException;
import za.co.twc.togetherness.womens.club.exception.InvalidContributionAmountException;
import za.co.twc.togetherness.womens.club.exception.NonActiveMemberContributionException;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@Transactional
public class ContributionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContributionService.class);

    private final ContributionRepository contributionRepository;
    private final MemberService memberService;

    @Autowired
    public ContributionService(ContributionRepository contributionRepository, MemberService memberService) {
        this.contributionRepository = contributionRepository;
        this.memberService = memberService;
    }

    public Contribution createContribution(Long memberId, Contribution contribution) {

        LOGGER.info("Initiate Create Contribution for member {}", memberId);

        Member member = memberService.getActiveMemberById(memberId);

        // Rule 1: Member must be ACTIVE
        if (member.getStatus() != MemberStatus.ACTIVE) {
            LOGGER.warn("Attempting to create contribution for non-active member {}", memberId);
            throw new NonActiveMemberContributionException(memberId);
        }

        // Rule 2: Amount must be valid
        if (contribution.getAmount() == null || contribution.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            LOGGER.warn("Invalid contribution amount for member {}", memberId);
            throw new InvalidContributionAmountException(contribution);
        }

        // Rule 3: Prevent duplicate contribution for the same month
        YearMonth currentMonth = YearMonth.now();

        boolean alreadyExists = contributionRepository.existsByMemberIdAndContributionMonth(memberId, currentMonth);

        if (alreadyExists) {

            LOGGER.warn("Duplicate contribution detected for member {} in same month", memberId);

            contribution.setMember(member);
            contribution.setContributionMonth(currentMonth);

            throw new DuplicateMonthlyContributionException(contribution);
        }

        // Set values
        contribution.setMember(member);
        contribution.setPaymentDate(
                contribution.getPaymentDate() != null
                        ? contribution.getPaymentDate()
                        : LocalDate.now()
        );
        contribution.setStatus(ContributionStatus.PAID);
        contribution.setContributionMonth(currentMonth);

        Contribution memberContributed = contributionRepository.save(contribution);

        LOGGER.info("Member {}, created contribution {} was completed successfully", memberId, memberContributed.getId());

        return memberContributed;
    }

    public List<Contribution> getAllContributions() {
        LOGGER.info("Get all contributions");
        return contributionRepository.findAllByOrderByPaymentDateDesc();
    }

    public List<Contribution> getContributionsByMemberId(Long memberId) {

        LOGGER.info("Get Contributions by member Id {}", memberId);

        return contributionRepository.findByMemberId(memberId);
    }

    public void generateMonthlyContributions() {

        LOGGER.info("Generate monthly contributions");

        List<Member> members = memberService.getAllActiveMembers();

        YearMonth currentMonth = YearMonth.now();

        for (Member member : members) {
            boolean exists = contributionRepository.existsByMemberIdAndContributionMonth(member.getId(), currentMonth);

            if (!exists) {
                Contribution contribution = new Contribution();
                contribution.setMember(member);
                contribution.setContributionMonth(currentMonth);
                contribution.setStatus(ContributionStatus.PENDING);
                contribution.setAmount(null);

                contributionRepository.save(contribution);

                LOGGER.info("Created PENDING contribution for member {} for month {}", member.getId(), currentMonth);
            }
        }

        LOGGER.info("Monthly contributions have been generated successfully");
    }

    @Transactional
    public void markMissedContributions() {
        LOGGER.info("Marking missed contributions for this month.");
        YearMonth currentMonth = YearMonth.now();

        List<Contribution> pending = contributionRepository.findByContributionMonthAndStatus(
                currentMonth,
                ContributionStatus.PENDING
        );

        for (Contribution contribution : pending) {
            contribution.setStatus(ContributionStatus.MISSED);
        }
    }
}
