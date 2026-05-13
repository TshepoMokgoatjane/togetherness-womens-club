package za.co.twc.togetherness.womens.club.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.exception.DependentNotFoundException;
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

    public void createDependent(Long memberId, Dependent dependent) {

        Member member = memberService.getActiveMemberById(memberId);

        dependent.setMember(member);
        dependent.setDeleted(false);

        Dependent saveDependent = dependentRepository.save(dependent);

        LOGGER.info("Dependent created: id={}, memberId={}", saveDependent.getId(), memberId);

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
