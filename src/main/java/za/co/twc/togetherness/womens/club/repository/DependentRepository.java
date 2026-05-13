package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.Dependent;

import java.util.List;

public interface DependentRepository extends JpaRepository<Dependent, Long> {

    List<Dependent> findByMemberIdAndDeletedFalse(Long memberId);
}
