package za.co.twc.togetherness.womens.club.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import za.co.twc.togetherness.womens.club.domain.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
}
