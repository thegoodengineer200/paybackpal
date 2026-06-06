package com.paybackpal.backend.borrower.repository;

import com.paybackpal.backend.borrower.entity.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BorrowerRepository extends JpaRepository<Borrower, UUID> {

    List<Borrower> findByOwnerUser_IdAndActiveTrueOrderByNameAsc(UUID ownerUserId);

    Optional<Borrower> findByIdAndOwnerUser_IdAndActiveTrue(UUID borrowerId, UUID ownerUserId);

    boolean existsByOwnerUser_IdAndPhoneNumberAndActiveTrue(
            UUID ownerUserId,
            String phoneNumber
    );

    boolean existsByOwnerUser_IdAndPhoneNumberAndActiveTrueAndIdNot(
            UUID ownerUserId,
            String phoneNumber,
            UUID borrowerId
    );

    List<Borrower> findByIdInAndOwnerUser_IdAndActiveTrue(
            List<UUID> borrowerIds, UUID ownerUserId
    );

}