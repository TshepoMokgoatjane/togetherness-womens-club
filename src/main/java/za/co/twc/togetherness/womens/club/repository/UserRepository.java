package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByMemberId(Long memberId);

    List<User> findByMemberIdIsNull();
}
