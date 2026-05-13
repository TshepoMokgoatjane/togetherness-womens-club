package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import za.co.twc.togetherness.womens.club.domain.Member;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByDeletedFalse();

    Optional<Member> findByIdAndDeletedFalse(Long id);

    @Query("SELECT MAX(m.id) FROM Member m")
    Long findMaxId();
}
