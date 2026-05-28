package za.co.twc.togetherness.womens.club.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import za.co.twc.togetherness.womens.club.domain.*;
import za.co.twc.togetherness.womens.club.event.ClaimStatusChangedEvent;
import za.co.twc.togetherness.womens.club.exception.MemberMissedLastThreeConsecutiveMonthsException;
import za.co.twc.togetherness.womens.club.repository.BurialClaimRepository;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BurialClaimService")
class BurialClaimServiceTest {

    @Mock
    private BurialClaimRepository burialClaimRepository;

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private BurialClaimService burialClaimService;

    private Member eligibleMember;

    @BeforeEach
    void setUp() {
        eligibleMember = new Member();
        eligibleMember.setId(1L);
        eligibleMember.setMemberNumber("MWC-000001");
        eligibleMember.setFirstName("Jane");
        eligibleMember.setLastName("Doe");
        eligibleMember.setStatus(MemberStatus.ACTIVE);
    }

    @Nested
    @DisplayName("isEligibleForClaim")
    class IsEligibleForClaim {

        @Test
        @DisplayName("should return true when member has paid all last 3 months")
        void shouldReturnTrueWhenAllThreeMonthsPaid() {
            YearMonth now = YearMonth.now();
            List<YearMonth> last3Months = List.of(
                    now.minusMonths(1),
                    now.minusMonths(2),
                    now.minusMonths(3)
            );

            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), eq(last3Months)))
                    .thenReturn(3L);

            boolean eligible = burialClaimService.isEligibleForClaim(eligibleMember);

            assertThat(eligible).isTrue();
        }

        @Test
        @DisplayName("should return false when member missed one month")
        void shouldReturnFalseWhenOnlyTwoMonthsPaid() {
            YearMonth now = YearMonth.now();
            List<YearMonth> last3Months = List.of(
                    now.minusMonths(1),
                    now.minusMonths(2),
                    now.minusMonths(3)
            );

            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), eq(last3Months)))
                    .thenReturn(2L);

            boolean eligible = burialClaimService.isEligibleForClaim(eligibleMember);

            assertThat(eligible).isFalse();
        }

        @Test
        @DisplayName("should return false when member has no paid months")
        void shouldReturnFalseWhenNoMonthsPaid() {
            YearMonth now = YearMonth.now();
            List<YearMonth> last3Months = List.of(
                    now.minusMonths(1),
                    now.minusMonths(2),
                    now.minusMonths(3)
            );

            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), eq(last3Months)))
                    .thenReturn(0L);

            boolean eligible = burialClaimService.isEligibleForClaim(eligibleMember);

            assertThat(eligible).isFalse();
        }
    }

    @Nested
    @DisplayName("createClaim")
    class CreateClaim {

        @Test
        @DisplayName("should create claim successfully when member is eligible")
        void shouldCreateClaimWhenEligible() {
            BurialClaim claim = new BurialClaim();
            claim.setDeceasedName("John Doe");
            claim.setClaimAmount(new BigDecimal("5000.00"));

            when(memberService.getActiveMemberById(1L)).thenReturn(eligibleMember);
            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), any()))
                    .thenReturn(3L);
            when(burialClaimRepository.save(any(BurialClaim.class))).thenAnswer(invocation -> {
                BurialClaim saved = invocation.getArgument(0);
                saved.setId(100L);
                return saved;
            });

            BurialClaim result = burialClaimService.createClaim(1L, claim);

            assertThat(result.getMember()).isEqualTo(eligibleMember);
            assertThat(result.getStatus()).isEqualTo(ClaimStatus.PENDING);
            assertThat(result.getClaimDate()).isEqualTo(LocalDate.now());
            assertThat(result.getDeceasedName()).isEqualTo("John Doe");
            verify(burialClaimRepository).save(any(BurialClaim.class));
        }

        @Test
        @DisplayName("should throw exception when member is not eligible (missed contributions)")
        void shouldThrowWhenMemberNotEligible() {
            BurialClaim claim = new BurialClaim();
            claim.setDeceasedName("John Doe");
            claim.setClaimAmount(new BigDecimal("5000.00"));

            when(memberService.getActiveMemberById(1L)).thenReturn(eligibleMember);
            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), any()))
                    .thenReturn(1L);

            assertThatThrownBy(() -> burialClaimService.createClaim(1L, claim))
                    .isInstanceOf(MemberMissedLastThreeConsecutiveMonthsException.class);

            verify(burialClaimRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when member has zero paid months")
        void shouldThrowWhenZeroPaidMonths() {
            BurialClaim claim = new BurialClaim();
            claim.setDeceasedName("John Doe");
            claim.setClaimAmount(new BigDecimal("5000.00"));

            when(memberService.getActiveMemberById(1L)).thenReturn(eligibleMember);
            when(contributionRepository.countPaidMonths(eq(1L), eq(ContributionStatus.PAID), any()))
                    .thenReturn(0L);

            assertThatThrownBy(() -> burialClaimService.createClaim(1L, claim))
                    .isInstanceOf(MemberMissedLastThreeConsecutiveMonthsException.class);

            verify(burialClaimRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("approveClaim")
    class ApproveClaim {

        @Test
        @DisplayName("should approve claim and publish event")
        void shouldApproveClaimAndPublishEvent() {
            BurialClaim claim = new BurialClaim();
            claim.setId(10L);
            claim.setStatus(ClaimStatus.PENDING);
            claim.setMember(eligibleMember);

            when(burialClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
            when(burialClaimRepository.save(any(BurialClaim.class))).thenAnswer(i -> i.getArgument(0));

            BurialClaim result = burialClaimService.approveClaim(10L);

            assertThat(result.getStatus()).isEqualTo(ClaimStatus.APPROVED);

            ArgumentCaptor<ClaimStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ClaimStatusChangedEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getClaimStatus()).isEqualTo(ClaimStatus.APPROVED);
        }

        @Test
        @DisplayName("should throw exception when claim not found")
        void shouldThrowWhenClaimNotFound() {
            when(burialClaimRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> burialClaimService.approveClaim(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Claim not found");
        }
    }

    @Nested
    @DisplayName("declineClaim")
    class DeclineClaim {

        @Test
        @DisplayName("should decline claim and publish event")
        void shouldDeclineClaimAndPublishEvent() {
            BurialClaim claim = new BurialClaim();
            claim.setId(10L);
            claim.setStatus(ClaimStatus.PENDING);
            claim.setMember(eligibleMember);

            when(burialClaimRepository.findById(10L)).thenReturn(Optional.of(claim));
            when(burialClaimRepository.save(any(BurialClaim.class))).thenAnswer(i -> i.getArgument(0));

            BurialClaim result = burialClaimService.declineClaim(10L);

            assertThat(result.getStatus()).isEqualTo(ClaimStatus.DECLINED);

            ArgumentCaptor<ClaimStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ClaimStatusChangedEvent.class);
            verify(applicationEventPublisher).publishEvent(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getClaimStatus()).isEqualTo(ClaimStatus.DECLINED);
        }

        @Test
        @DisplayName("should throw exception when claim not found")
        void shouldThrowWhenClaimNotFound() {
            when(burialClaimRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> burialClaimService.declineClaim(999L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Claim not found");
        }
    }
}
