package za.co.twc.togetherness.womens.club.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.*;
import za.co.twc.togetherness.womens.club.repository.MemberRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService")
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private DependentService dependentService;

    @InjectMocks
    private MemberService memberService;

    private Member activeMember;

    @BeforeEach
    void setUp() {
        activeMember = new Member();
        activeMember.setId(1L);
        activeMember.setMemberNumber("MWC-000001");
        activeMember.setFirstName("Jane");
        activeMember.setLastName("Doe");
        activeMember.setEmail("jane@example.com");
        activeMember.setStatus(MemberStatus.ACTIVE);
        activeMember.setDeleted(false);
    }

    @Nested
    @DisplayName("createMember")
    class CreateMember {

        @Test
        @DisplayName("should create member with generated member number")
        void shouldCreateMemberWithGeneratedNumber() {
            Member newMember = new Member();
            newMember.setFirstName("Alice");
            newMember.setLastName("Wonder");
            newMember.setEmail("alice@example.com");

            when(memberRepository.existsByEmail("alice@example.com")).thenReturn(false);
            when(memberRepository.findMaxId()).thenReturn(5L);
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            memberService.createMember(newMember);

            assertThat(newMember.getMemberNumber()).isEqualTo("MWC-000006");
            assertThat(newMember.getStatus()).isEqualTo(MemberStatus.ACTIVE);
            assertThat(newMember.isDeleted()).isFalse();
            verify(memberRepository).save(newMember);
        }

        @Test
        @DisplayName("should generate MWC-000001 when no members exist")
        void shouldGenerateFirstMemberNumber() {
            Member newMember = new Member();
            newMember.setFirstName("First");
            newMember.setLastName("Member");
            newMember.setEmail("first@example.com");

            when(memberRepository.existsByEmail("first@example.com")).thenReturn(false);
            when(memberRepository.findMaxId()).thenReturn(null);
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            memberService.createMember(newMember);

            assertThat(newMember.getMemberNumber()).isEqualTo("MWC-000001");
        }

        @Test
        @DisplayName("should throw exception when email already exists")
        void shouldThrowWhenDuplicateEmail() {
            Member newMember = new Member();
            newMember.setEmail("jane@example.com");

            when(memberRepository.existsByEmail("jane@example.com")).thenReturn(true);

            assertThatThrownBy(() -> memberService.createMember(newMember))
                    .isInstanceOf(DuplicateEmailAddressException.class);

            verify(memberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("updateMember")
    class UpdateMember {

        @Test
        @DisplayName("should update active member successfully")
        void shouldUpdateActiveMember() {
            Member updatedData = new Member();
            updatedData.setFirstName("Janet");
            updatedData.setLastName("Doe");
            updatedData.setEmail("jane@example.com");
            updatedData.setPhoneNumber("0821234567");
            updatedData.setPhysicalAddress("123 New Street");

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            Member result = memberService.updateMember(1L, updatedData);

            assertThat(result.getFirstName()).isEqualTo("Janet");
            verify(memberRepository).save(activeMember);
        }

        @Test
        @DisplayName("should throw exception when updating inactive member")
        void shouldThrowWhenUpdatingInactiveMember() {
            activeMember.setStatus(MemberStatus.INACTIVE);

            Member updatedData = new Member();
            updatedData.setFirstName("Janet");
            updatedData.setEmail("jane@example.com");

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberService.updateMember(1L, updatedData))
                    .isInstanceOf(MemberInactiveException.class);
        }

        @Test
        @DisplayName("should throw exception when updating deceased member")
        void shouldThrowWhenUpdatingDeceasedMember() {
            activeMember.setStatus(MemberStatus.DECEASED);

            Member updatedData = new Member();
            updatedData.setFirstName("Janet");
            updatedData.setEmail("jane@example.com");

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberService.updateMember(1L, updatedData))
                    .isInstanceOf(MemberDeceasedException.class);
        }

        @Test
        @DisplayName("should throw exception when changing to duplicate email")
        void shouldThrowWhenDuplicateEmailOnUpdate() {
            Member updatedData = new Member();
            updatedData.setFirstName("Janet");
            updatedData.setEmail("other@example.com");

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
            when(memberRepository.existsByEmail("other@example.com")).thenReturn(true);

            assertThatThrownBy(() -> memberService.updateMember(1L, updatedData))
                    .isInstanceOf(DuplicateEmailAddressException.class);
        }
    }

    @Nested
    @DisplayName("softDeleteMember")
    class SoftDeleteMember {

        @Test
        @DisplayName("should soft delete active member with no dependents")
        void shouldSoftDeleteActiveMember() {
            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
            when(dependentService.getDependentsByMemberId(1L)).thenReturn(List.of());
            when(memberRepository.save(any(Member.class))).thenAnswer(i -> i.getArgument(0));

            memberService.softDeleteMember(1L);

            assertThat(activeMember.isDeleted()).isTrue();
            verify(memberRepository).save(activeMember);
        }

        @Test
        @DisplayName("should throw exception when deleting deceased member")
        void shouldThrowWhenDeletingDeceasedMember() {
            activeMember.setStatus(MemberStatus.DECEASED);

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberService.softDeleteMember(1L))
                    .isInstanceOf(MemberDeceasedException.class);
        }

        @Test
        @DisplayName("should throw exception when deleting inactive member")
        void shouldThrowWhenDeletingInactiveMember() {
            activeMember.setStatus(MemberStatus.INACTIVE);

            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));

            assertThatThrownBy(() -> memberService.softDeleteMember(1L))
                    .isInstanceOf(MemberInactiveException.class);
        }

        @Test
        @DisplayName("should throw exception when member has dependents")
        void shouldThrowWhenMemberHasDependents() {
            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));
            when(dependentService.getDependentsByMemberId(1L)).thenReturn(List.of(new za.co.twc.togetherness.womens.club.domain.Dependent()));

            assertThatThrownBy(() -> memberService.softDeleteMember(1L))
                    .isInstanceOf(MemberHasDependentsException.class);
        }
    }

    @Nested
    @DisplayName("getActiveMemberById")
    class GetActiveMemberById {

        @Test
        @DisplayName("should return member when found")
        void shouldReturnMemberWhenFound() {
            when(memberRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(activeMember));

            Member result = memberService.getActiveMemberById(1L);

            assertThat(result).isEqualTo(activeMember);
        }

        @Test
        @DisplayName("should throw exception when member not found")
        void shouldThrowWhenMemberNotFound() {
            when(memberRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> memberService.getActiveMemberById(999L))
                    .isInstanceOf(MemberNotFoundException.class);
        }
    }
}
