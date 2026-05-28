package za.co.twc.togetherness.womens.club.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.twc.togetherness.womens.club.domain.*;
import za.co.twc.togetherness.womens.club.exception.DuplicateMonthlyContributionException;
import za.co.twc.togetherness.womens.club.exception.InvalidContributionAmountException;
import za.co.twc.togetherness.womens.club.exception.NonActiveMemberContributionException;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ContributionService")
class ContributionServiceTest {

    @Mock
    private ContributionRepository contributionRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private ContributionService contributionService;

    private Member activeMember;
    private Member inactiveMember;

    @BeforeEach
    void setUp() {
        activeMember = new Member();
        activeMember.setId(1L);
        activeMember.setMemberNumber("MWC-000001");
        activeMember.setFirstName("Jane");
        activeMember.setLastName("Doe");
        activeMember.setStatus(MemberStatus.ACTIVE);

        inactiveMember = new Member();
        inactiveMember.setId(2L);
        inactiveMember.setMemberNumber("MWC-000002");
        inactiveMember.setFirstName("Mary");
        inactiveMember.setLastName("Smith");
        inactiveMember.setStatus(MemberStatus.INACTIVE);
    }

    @Nested
    @DisplayName("createContribution")
    class CreateContribution {

        @Test
        @DisplayName("should create contribution successfully for active member")
        void shouldCreateContributionForActiveMember() {
            Contribution contribution = new Contribution();
            contribution.setAmount(new BigDecimal("100.00"));

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(1L), any(YearMonth.class)))
                    .thenReturn(false);
            when(contributionRepository.save(any(Contribution.class))).thenAnswer(invocation -> {
                Contribution saved = invocation.getArgument(0);
                saved.setId(10L);
                return saved;
            });

            Contribution result = contributionService.createContribution(1L, contribution);

            assertThat(result.getMember()).isEqualTo(activeMember);
            assertThat(result.getStatus()).isEqualTo(ContributionStatus.PAID);
            assertThat(result.getContributionMonth()).isEqualTo(YearMonth.now());
            assertThat(result.getPaymentDate()).isEqualTo(LocalDate.now());
            verify(contributionRepository).save(any(Contribution.class));
        }

        @Test
        @DisplayName("should use provided payment date when set")
        void shouldUseProvidedPaymentDate() {
            LocalDate customDate = LocalDate.of(2026, 5, 15);
            Contribution contribution = new Contribution();
            contribution.setAmount(new BigDecimal("100.00"));
            contribution.setPaymentDate(customDate);

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(1L), any(YearMonth.class)))
                    .thenReturn(false);
            when(contributionRepository.save(any(Contribution.class))).thenAnswer(i -> i.getArgument(0));

            Contribution result = contributionService.createContribution(1L, contribution);

            assertThat(result.getPaymentDate()).isEqualTo(customDate);
        }

        @Test
        @DisplayName("should throw exception when member is not active")
        void shouldThrowWhenMemberNotActive() {
            Contribution contribution = new Contribution();
            contribution.setAmount(new BigDecimal("100.00"));

            when(memberService.getActiveMemberById(2L)).thenReturn(inactiveMember);

            assertThatThrownBy(() -> contributionService.createContribution(2L, contribution))
                    .isInstanceOf(NonActiveMemberContributionException.class);

            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when amount is null")
        void shouldThrowWhenAmountIsNull() {
            Contribution contribution = new Contribution();
            contribution.setAmount(null);
            contribution.setMember(activeMember);

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);

            assertThatThrownBy(() -> contributionService.createContribution(1L, contribution))
                    .isInstanceOf(InvalidContributionAmountException.class);

            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when amount is zero")
        void shouldThrowWhenAmountIsZero() {
            Contribution contribution = new Contribution();
            contribution.setAmount(BigDecimal.ZERO);
            contribution.setMember(activeMember);

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);

            assertThatThrownBy(() -> contributionService.createContribution(1L, contribution))
                    .isInstanceOf(InvalidContributionAmountException.class);

            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when amount is negative")
        void shouldThrowWhenAmountIsNegative() {
            Contribution contribution = new Contribution();
            contribution.setAmount(new BigDecimal("-50.00"));
            contribution.setMember(activeMember);

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);

            assertThatThrownBy(() -> contributionService.createContribution(1L, contribution))
                    .isInstanceOf(InvalidContributionAmountException.class);

            verify(contributionRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when duplicate contribution exists for same month")
        void shouldThrowWhenDuplicateContributionForMonth() {
            Contribution contribution = new Contribution();
            contribution.setAmount(new BigDecimal("100.00"));

            when(memberService.getActiveMemberById(1L)).thenReturn(activeMember);
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(1L), any(YearMonth.class)))
                    .thenReturn(true);

            assertThatThrownBy(() -> contributionService.createContribution(1L, contribution))
                    .isInstanceOf(DuplicateMonthlyContributionException.class);

            verify(contributionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("generateMonthlyContributions")
    class GenerateMonthlyContributions {

        @Test
        @DisplayName("should create pending contributions for all active members without existing contribution")
        void shouldCreatePendingContributionsForActiveMembers() {
            Member member2 = new Member();
            member2.setId(3L);
            member2.setStatus(MemberStatus.ACTIVE);

            when(memberService.getAllActiveMembers()).thenReturn(List.of(activeMember, member2));
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(1L), any(YearMonth.class)))
                    .thenReturn(false);
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(3L), any(YearMonth.class)))
                    .thenReturn(false);
            when(contributionRepository.save(any(Contribution.class))).thenAnswer(i -> i.getArgument(0));

            contributionService.generateMonthlyContributions();

            verify(contributionRepository, times(2)).save(argThat(contribution ->
                    contribution.getStatus() == ContributionStatus.PENDING
                            && contribution.getAmount() == null
                            && contribution.getContributionMonth().equals(YearMonth.now())
            ));
        }

        @Test
        @DisplayName("should skip members who already have a contribution for the month")
        void shouldSkipMembersWithExistingContribution() {
            when(memberService.getAllActiveMembers()).thenReturn(List.of(activeMember));
            when(contributionRepository.existsByMemberIdAndContributionMonth(eq(1L), any(YearMonth.class)))
                    .thenReturn(true);

            contributionService.generateMonthlyContributions();

            verify(contributionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("markMissedContributions")
    class MarkMissedContributions {

        @Test
        @DisplayName("should mark all pending contributions for current month as missed")
        void shouldMarkPendingAsMissed() {
            Contribution pending1 = new Contribution();
            pending1.setStatus(ContributionStatus.PENDING);
            Contribution pending2 = new Contribution();
            pending2.setStatus(ContributionStatus.PENDING);

            when(contributionRepository.findByContributionMonthAndStatus(
                    YearMonth.now(), ContributionStatus.PENDING))
                    .thenReturn(List.of(pending1, pending2));

            contributionService.markMissedContributions();

            assertThat(pending1.getStatus()).isEqualTo(ContributionStatus.MISSED);
            assertThat(pending2.getStatus()).isEqualTo(ContributionStatus.MISSED);
        }

        @Test
        @DisplayName("should handle empty list gracefully")
        void shouldHandleNoPendingContributions() {
            when(contributionRepository.findByContributionMonthAndStatus(
                    YearMonth.now(), ContributionStatus.PENDING))
                    .thenReturn(List.of());

            contributionService.markMissedContributions();

            // No exception thrown
        }
    }

    @Nested
    @DisplayName("getTotalContributionsForTheMonth")
    class GetTotalContributions {

        @Test
        @DisplayName("should sum all paid contributions for the month")
        void shouldSumPaidContributions() {
            Contribution c1 = new Contribution();
            c1.setAmount(new BigDecimal("100.00"));
            Contribution c2 = new Contribution();
            c2.setAmount(new BigDecimal("150.00"));

            YearMonth month = YearMonth.of(2026, 5);
            when(contributionRepository.findByContributionMonthAndStatus(month, ContributionStatus.PAID))
                    .thenReturn(List.of(c1, c2));

            BigDecimal total = contributionService.getTotalContributionsForTheMonth(month);

            assertThat(total).isEqualByComparingTo(new BigDecimal("250.00"));
        }

        @Test
        @DisplayName("should return zero when no paid contributions exist")
        void shouldReturnZeroWhenNoPaidContributions() {
            YearMonth month = YearMonth.of(2026, 5);
            when(contributionRepository.findByContributionMonthAndStatus(month, ContributionStatus.PAID))
                    .thenReturn(List.of());

            BigDecimal total = contributionService.getTotalContributionsForTheMonth(month);

            assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getLast6MonthsTotal")
    class GetLast6MonthsTotal {

        @Test
        @DisplayName("should return totals for last 6 months in order")
        void shouldReturnLast6MonthsTotals() {
            when(contributionRepository.findByContributionMonthAndStatus(any(YearMonth.class), eq(ContributionStatus.PAID)))
                    .thenReturn(List.of());

            var result = contributionService.getLast6MonthsTotal();

            assertThat(result).hasSize(6);

            // Verify ordering: oldest first
            YearMonth current = YearMonth.now();
            List<String> expectedKeys = List.of(
                    current.minusMonths(5).toString(),
                    current.minusMonths(4).toString(),
                    current.minusMonths(3).toString(),
                    current.minusMonths(2).toString(),
                    current.minusMonths(1).toString(),
                    current.toString()
            );
            assertThat(result.keySet()).containsExactlyElementsOf(expectedKeys);
        }
    }
}
