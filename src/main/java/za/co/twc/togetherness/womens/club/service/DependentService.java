package za.co.twc.togetherness.womens.club.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.*;
import za.co.twc.togetherness.womens.club.repository.DependentRepository;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DependentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependentService.class);

    private final DependentRepository dependentRepository;
    private final MemberService memberService;

    public DependentService(DependentRepository dependentRepository, MemberService memberService) {
        this.dependentRepository = dependentRepository;
        this.memberService = memberService;
    }

    public Dependent createDependent(Long memberId, Dependent dependent) {

        // Check if an active dependent with this ID number already exists
        if (dependentRepository.findByIdNumberAndDeletedFalse(dependent.getIdNumber()).isPresent()) {
            throw new DuplicateDependentException(dependent.getIdNumber());
        }

        Member member = memberService.getActiveMemberById(memberId);

        MemberStatus memberStatus = member.getStatus();

        if (memberStatus == MemberStatus.INACTIVE) {
            LOGGER.warn("Member with id {} is not active and cannot be modified", memberId);
            throw new MemberInactiveException(memberId);
        } else if (memberStatus == MemberStatus.DECEASED) {
            LOGGER.warn("Member with id {} is already deceased", memberId);
            throw new MemberDeceasedException(memberId);
        } else if (memberStatus != MemberStatus.ACTIVE) {
            LOGGER.warn("Member with id {} is not active. Therefore cannot have dependents", memberId);
            throw new MemberCannotAddDependentsException(memberId);
        }

        // Check if a soft-deleted dependent with this ID number exists — reactivate it
        Optional<Dependent> softDeleted = dependentRepository.findByIdNumberAndDeletedTrue(dependent.getIdNumber());
        if (softDeleted.isPresent()) {
            Dependent existing = softDeleted.get();
            existing.setFirstName(dependent.getFirstName());
            existing.setLastName(dependent.getLastName());
            existing.setEmail(dependent.getEmail());
            existing.setPhoneNumber(dependent.getPhoneNumber());
            existing.setAlternativePhoneNumber(dependent.getAlternativePhoneNumber());
            existing.setPhysicalAddress(dependent.getPhysicalAddress());
            existing.setRelationship(dependent.getRelationship());
            existing.setMember(member);
            existing.setDeleted(false);

            Dependent reactivated = dependentRepository.save(existing);
            LOGGER.info("Dependent reactivated: id={}, memberId={}", reactivated.getId(), memberId);
            return reactivated;
        }

        dependent.setMember(member);
        dependent.setDeleted(false);

        Dependent savedDependent = dependentRepository.save(dependent);
        LOGGER.info("Dependent created: id={}, memberId={}", savedDependent.getId(), memberId);

        return savedDependent;
    }

    public List<Dependent> getDependentsByMemberId(Long memberId) {
        return dependentRepository.findByMemberIdAndDeletedFalse(memberId);
    }

    public void softDeleteDependent(Long id) {
        Dependent dependent = dependentRepository.findById(id)
                .orElseThrow(() -> new DependentNotFoundException(id));

        dependent.setDeleted(true);
        dependentRepository.save(dependent);
        LOGGER.info("Dependent deleted: id={}, memberId={}", dependent.getId(), dependent.getMember().getId());
    }
}
