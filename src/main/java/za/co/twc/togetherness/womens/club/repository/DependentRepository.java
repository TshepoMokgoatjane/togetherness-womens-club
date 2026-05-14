package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.Dependent;

import java.util.List;
import java.util.Optional;

public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByMemberIdAndDeletedFalse(Long memberId);

    Optional<Dependent> findByIdNumberAndDeletedFalse(String idNumber);

    Optional<Dependent> findByIdNumberAndDeletedTrue(String idNumber);
}
