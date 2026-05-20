package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.MemberStatus;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {

    List<Member> findByDeletedFalse();

    Page<Member> findByDeletedFalse(Pageable pageable);

    Optional<Member> findByIdAndDeletedFalse(Long id);

    @Query("SELECT MAX(m.id) FROM Member m")
    Long findMaxId();

    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.deleted = false AND " +
           "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.memberNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Member> searchMembers(@Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.deleted = false AND m.status = :status")
    Page<Member> findByStatus(@Param("status") MemberStatus status, Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.deleted = false AND " +
           "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.memberNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "AND m.status = :status")
    Page<Member> searchMembersWithStatus(@Param("search") String search, @Param("status") MemberStatus status, Pageable pageable);
}
