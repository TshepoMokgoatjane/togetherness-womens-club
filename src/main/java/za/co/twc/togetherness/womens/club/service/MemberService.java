package za.co.twc.togetherness.womens.club.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.MemberNotFoundException;
import za.co.twc.togetherness.womens.club.repository.MemberRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    // ==================
    // CREATE
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

    // ==================
    // READ
    // ==================
    @Transactional(readOnly = true)
    public List<Member> getAllActiveMembers() {
        return memberRepository.findByDeletedFalse();
    }

    @Transactional(readOnly = true)
    public Member getActiveMemberById(Long id) {
        return memberRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }


    // ==================
    // UPDATE
    // ==================
    public Member updateMember(Long id, Member updatedMember) {
        Member existingMember = getActiveMemberById(id);

        existingMember.setFirstName(updatedMember.getFirstName());
        existingMember.setLastName(updatedMember.getLastName());
        existingMember.setPhoneNumber(updatedMember.getPhoneNumber());
        existingMember.setAlternativePhoneNumber(updatedMember.getAlternativePhoneNumber());
        existingMember.setEmail(updatedMember.getEmail());
        existingMember.setPhysicalAddress(updatedMember.getPhysicalAddress());

        return memberRepository.save(existingMember);
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

    // ==================
    // SOFT DELETE
    // ==================
    public void softDeleteMember(Long id) {
        Member member = getActiveMemberById(id);
        member.setDeleted(true);
        memberRepository.save(member);

        LOGGER.warn("Member soft-deleted: memberNumber={}, status={}", member.getMemberNumber(), member.getStatus());
    }

}