package za.co.twc.togetherness.womens.club.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.MemberDeceasedException;
import za.co.twc.togetherness.womens.club.exception.MemberHasDependentsException;
import za.co.twc.togetherness.womens.club.exception.MemberInactiveException;
import za.co.twc.togetherness.womens.club.exception.MemberNotFoundException;
import za.co.twc.togetherness.womens.club.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    private final DependentService dependentService;

    @Autowired
    public MemberService(MemberRepository memberRepository, @Lazy DependentService dependentService) {
        this.memberRepository = memberRepository;
        this.dependentService = dependentService;
    }

    // =============================
    // READ - GET ALL ACTIVE MEMBERS
    // =============================
    @Transactional(readOnly = true)
    public List<Member> getAllActiveMembers() {
        return memberRepository.findByDeletedFalse();
    }

    // ==============================
    // READ - GET ACTIVE MEMBER BY ID
    // ==============================
    @Transactional(readOnly = true)
    public Member getActiveMemberById(Long id) {
        return memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    // ==================
    // CREATE MEMBER
    // ==================
    public void createMember(Member member) {

        String nextMemberNumber = generateMemberNumber();

        member.setMemberNumber(nextMemberNumber);
        member.setStatus(MemberStatus.ACTIVE);
        member.setDeleted(false);

        if (member.getJoinDate() == null) {
            member.setJoinDate(LocalDate.now());
        }

        Member savedMember = memberRepository.save(member);

        LOGGER.info("Member created: memberNumber={}, status={}", savedMember.getMemberNumber(), savedMember.getStatus());
    }

    // Generate Main Member Number
    private String generateMemberNumber() {
        Long maxId = memberRepository.findMaxId();
        Long nextNumber = (maxId == null) ? 1 : maxId + 1;
        return String.format("MWC-%06d", nextNumber);
    }

    // ===============================
    // UPDATE MEMBER WITH STATUS CHECK
    // ===============================
    public Member updateMember(Long id, Member updatedMember) {

        Member existingMember = getActiveMemberById(id);

        MemberStatus memberStatus = existingMember.getStatus();

        if (memberStatus == MemberStatus.ACTIVE) {
            LOGGER.warn("Attempting to update an INACTIVE member with id {}", id);
            throw new MemberInactiveException(id);
        }

        if (memberStatus == MemberStatus.DECEASED) {
            LOGGER.warn("Attempting to update a DECEASED member with id {}", id);
            throw new MemberDeceasedException(id);
        }

        // Allowed only if ACTIVE
        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
        existingMember.setAlternativePhoneNumber(updatedMember.getAlternativePhoneNumber());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setPhysicalAddress(updatedMember.getPhysicalAddress());

        Member newlyUpdatedMember = memberRepository.save(existingMember);

        LOGGER.info("Member updated: id={}, status={}", id, newlyUpdatedMember.getStatus());

        return newlyUpdatedMember;
    }

    // ==================
    // STATUS MANAGEMENT
    // ==================
    public void changeMemberStatus(Long id, MemberStatus status) {
        Member member = getActiveMemberById(id);
        MemberStatus previousStatus = member.getStatus();

        member.setStatus(status);
        memberRepository.save(member);

        LOGGER.info("Member status changed: memberNumber={}, from={}, to={}", member.getMemberNumber(),
                previousStatus, status);
    }

    // ====================================
    // DELETE MEMBER (SOFT) WITH FULL RULES
    // ====================================
    public void softDeleteMember(Long id) {

        Member member = getActiveMemberById(id);

        MemberStatus memberStatus = member.getStatus();

        // STATUS RULES
        if (memberStatus == MemberStatus.DECEASED) {
            LOGGER.warn("Attempting to delete DECEASED member with id {}", id);
            throw new MemberDeceasedException(id);
        }

        if (memberStatus == MemberStatus.INACTIVE) {
            LOGGER.warn("Attempting to delete INACTIVE member with id {}", id);
            throw new MemberInactiveException(id);
        }

        List<Dependent> dependents = dependentService.getDependentsByMemberId(id);

        // BUSINESS RULE: Cannot delete if dependents exist
        if (!dependents.isEmpty()) {
            LOGGER.error("Attempting to delete member with id {}, while dependents exist!", id);
            throw new MemberHasDependentsException(id);
        }

        // SAFE DELETE
        member.setDeleted(true);
        memberRepository.save(member);

        LOGGER.info("Member soft-deleted: memberNumber={}, status={}", member.getMemberNumber(), member.getStatus());
    }

}