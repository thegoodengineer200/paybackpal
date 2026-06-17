package com.paybackpal.backend.notification.repository;

import com.paybackpal.backend.notification.entity.NotificationOutbox;
import com.paybackpal.backend.notification.entity.NotificationStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationOutboxRepository extends JpaRepository<NotificationOutbox, UUID> {

    List<NotificationOutbox> findTop50ByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status, OffsetDateTime scheduledAt
    );

    List<NotificationOutbox> findByStatusAndScheduledAtLessThanEqualOrderByScheduledAtAsc(
            NotificationStatus status, OffsetDateTime scheduledAt, Pageable pageable
    );
}
