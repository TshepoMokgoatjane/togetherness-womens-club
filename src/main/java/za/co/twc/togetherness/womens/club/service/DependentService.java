package za.co.twc.togetherness.womens.club.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;
import za.co.twc.togetherness.womens.club.exception.DependentNotFoundException;
import za.co.twc.togetherness.womens.club.exception.MemberInactiveException;
import za.co.twc.togetherness.womens.club.exception.MemberCannotAddDependentsException;
import za.co.twc.togetherness.womens.club.exception.MemberDeceasedException;
import za.co.twc.togetherness.womens.club.repository.DependentRepository;

import java.util.List;

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

        dependent.setMember(member);
        dependent.setDeleted(false);

        Dependent saveDependent = dependentRepository.save(dependent);

        LOGGER.info("Dependent created: id={}, memberId={}", saveDependent.getId(), memberId);

        return saveDependent;

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
