package za.co.twc.togetherness.womens.club.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.exception.MemberMissedLastThreeConsecutiveMonthsException;
import za.co.twc.togetherness.womens.club.repository.BurialClaimRepository;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;

import java.time.LocalDate;
import java.time.YearMonth;

@Service
public class BurialClaimService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BurialClaimService.class);

    private final BurialClaimRepository burialClaimRepository;

    private final ContributionRepository contributionRepository;

    private final MemberService memberService;

    @Autowired
    public BurialClaimService(BurialClaimRepository burialClaimRepository, ContributionRepository contributionRepository, MemberService memberService) {
        this.burialClaimRepository = burialClaimRepository;
        this.contributionRepository = contributionRepository;
        this.memberService = memberService;
    }

    public java.util.List<BurialClaim> getAllClaims() {
        LOGGER.info("Get all burial claims");
        return burialClaimRepository.findAll();
    }

    public java.util.List<BurialClaim> getClaimsByMemberId(Long memberId) {
        LOGGER.info("Get claims for member {}", memberId);
        return burialClaimRepository.findByMemberId(memberId);
    }

    public boolean hasPaidLast3ConsecutiveMonths(Member member) {

        YearMonth now = YearMonth.now();

        for (int i = 1; i <= 3; i++) {
            YearMonth checkMonth = now.minusMonths(i);

            boolean paid = contributionRepository.findByContributionMonthAndStatus(checkMonth, ContributionStatus.PAID)
                    .stream()
                    .anyMatch(contribution -> contribution.getMember().getId().equals(member.getId()));

            if (!paid) {
                return false;
            }
        }
        return true;
    }

    public BurialClaim createClaim(Long memberId, BurialClaim burialClaim) {

        LOGGER.info("Creating claim with id {}", burialClaim.getId());

        Member member = memberService.getActiveMemberById(memberId);

        if (!hasPaidLast3ConsecutiveMonths(member)) {
            LOGGER.info("Member with ID {} not eligible for claim/payout, member missed last 3 consecutive months", member.getId());
            throw new MemberMissedLastThreeConsecutiveMonthsException(member);
        }
        burialClaim.setMember(member);
        burialClaim.setClaimDate(LocalDate.now());
        burialClaim.setStatus(ClaimStatus.PENDING);

        BurialClaim saved = burialClaimRepository.save(burialClaim);

        LOGGER.info("Created claim with id {}", saved.getId());

        return saved;
    }

    public BurialClaim approveClaim(Long claimId) {
        BurialClaim claim = burialClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id " + claimId));

        claim.setStatus(ClaimStatus.APPROVED);
        BurialClaim saved = burialClaimRepository.save(claim);

        LOGGER.info("Claim {} approved for member {}", claimId, claim.getMember().getMemberNumber());
        return saved;
    }

    public BurialClaim declineClaim(Long claimId) {
        BurialClaim claim = burialClaimRepository.findById(claimId)
                .orElseThrow(() -> new RuntimeException("Claim not found with id " + claimId));

        claim.setStatus(ClaimStatus.DECLINED);
        BurialClaim saved = burialClaimRepository.save(claim);

        LOGGER.info("Claim {} declined for member {}", claimId, claim.getMember().getMemberNumber());
        return saved;
    }

    public Page<BurialClaim> getPaginatedClaims(int page, int size, String sortBy) {
        return burialClaimRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy).descending()));
    }
}
