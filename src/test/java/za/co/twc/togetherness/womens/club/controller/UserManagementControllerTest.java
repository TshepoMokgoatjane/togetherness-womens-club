package za.co.twc.togetherness.womens.club.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;
import za.co.twc.togetherness.womens.club.service.MemberService;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserManagementController")
class UserManagementControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private UserManagementController controller;

    private User testUser;
    private Member testMember;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRole("USER");

        testMember = new Member();
        testMember.setId(2L);
        testMember.setMemberNumber("MWC-000002");
        testMember.setFirstName("Jane");
        testMember.setLastName("Doe");
        testMember.setStatus(MemberStatus.ACTIVE);
    }

    @Nested
    @DisplayName("listUsers")
    class ListUsers {

        @Test
        @DisplayName("should populate model with users and members")
        void shouldPopulateModel() {
            Model model = new ExtendedModelMap();
            when(userRepository.findAll()).thenReturn(List.of(testUser));
            when(memberService.getAllActiveMembers()).thenReturn(List.of(testMember));

            String view = controller.listUsers(model);

            assertThat(view).isEqualTo("admin/users");
            assertThat(model.getAttribute("users")).isEqualTo(List.of(testUser));
            assertThat(model.getAttribute("members")).isEqualTo(List.of(testMember));
        }
    }

    @Nested
    @DisplayName("linkUserToMember")
    class LinkUser {

        @Test
        @DisplayName("should link user to member successfully")
        void shouldLinkSuccessfully() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findByMemberId(2L)).thenReturn(Optional.empty());
            when(memberService.getActiveMemberById(2L)).thenReturn(testMember);

            String result = controller.linkUserToMember(1L, 2L, redirectAttributes);

            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute(eq("successMessage"), argThat((String s) -> s.contains("testuser")));
            verify(userRepository).save(testUser);
            assertThat(testUser.getMemberId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("should reject when member already linked to another user")
        void shouldRejectDuplicateLink() {
            User otherUser = new User();
            otherUser.setId(99L);
            otherUser.setUsername("otheruser");

            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(userRepository.findByMemberId(2L)).thenReturn(Optional.of(otherUser));

            String result = controller.linkUserToMember(1L, 2L, redirectAttributes);

            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), argThat((String s) -> s.contains("already linked")));
            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("should show error when user not found")
        void shouldShowErrorWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            String result = controller.linkUserToMember(999L, 2L, redirectAttributes);

            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute("errorMessage", "User not found.");
        }
    }

    @Nested
    @DisplayName("unlinkUser")
    class UnlinkUser {

        @Test
        @DisplayName("should unlink user from member")
        void shouldUnlinkSuccessfully() {
            testUser.setMemberId(5L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

            String result = controller.unlinkUser(1L, redirectAttributes);

            assertThat(result).isEqualTo("redirect:/admin/users");
            assertThat(testUser.getMemberId()).isNull();
            verify(userRepository).save(testUser);
            verify(redirectAttributes).addFlashAttribute(eq("successMessage"), argThat((String s) -> s.contains("unlinked")));
        }

        @Test
        @DisplayName("should show error when user not found on unlink")
        void shouldShowErrorWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            String result = controller.unlinkUser(999L, redirectAttributes);

            assertThat(result).isEqualTo("redirect:/admin/users");
            verify(redirectAttributes).addFlashAttribute("errorMessage", "User not found.");
        }
    }
}
