package com.paybackpal.backend.borrower.service;

import com.paybackpal.backend.auth.service.CurrentUserService;
import com.paybackpal.backend.borrower.dto.BorrowerResponse;
import com.paybackpal.backend.borrower.dto.CreateBorrowerRequest;
import com.paybackpal.backend.borrower.dto.UpdateBorrowerRequest;
import com.paybackpal.backend.borrower.entity.Borrower;
import com.paybackpal.backend.borrower.repository.BorrowerRepository;
import com.paybackpal.backend.common.exception.DuplicateResourceException;
import com.paybackpal.backend.common.exception.ResourceNotFoundException;
import com.paybackpal.backend.user.entity.AppUser;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BorrowerService {
    private final BorrowerRepository borrowerRepository;
    private final CurrentUserService currentUserService;

    public BorrowerService(
            BorrowerRepository borrowerRepository,
            CurrentUserService currentUserService
    ) {
        this.borrowerRepository = borrowerRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional
    public BorrowerResponse createBorrower(CreateBorrowerRequest request) {
        AppUser currentUser = currentUserService.getCurrentUser();

        String normalizedName = normalizeRequired(request.getName());
        String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());

        if (borrowerRepository.existsByOwnerUser_IdAndPhoneNumberAndActiveTrue(
                currentUser.getId(),
                normalizedPhoneNumber
        )) {
            throw new DuplicateResourceException("Borrower with this phone number already exists");
        }

        Borrower borrower = new Borrower(
                currentUser,
                normalizedName,
                normalizedPhoneNumber
        );

        try {
            Borrower savedBorrower = borrowerRepository.save(borrower);
            return BorrowerResponse.from(savedBorrower);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Borrower with this phone number already exists");
        }
    }

    @Transactional(readOnly = true)
    public List<BorrowerResponse> getCurrentUserBorrowers() {
        AppUser currentUser = currentUserService.getCurrentUser();

        return borrowerRepository
                .findByOwnerUser_IdAndActiveTrueOrderByNameAsc(currentUser.getId())
                .stream()
                .map(BorrowerResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public BorrowerResponse getBorrower(UUID borrowerId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Borrower borrower = getActiveBorrowerForUser(
                borrowerId,
                currentUser.getId()
        );

        return BorrowerResponse.from(borrower);
    }

    @Transactional
    public BorrowerResponse updateBorrower(
            UUID borrowerId,
            UpdateBorrowerRequest request
    ) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Borrower borrower = getActiveBorrowerForUser(
                borrowerId,
                currentUser.getId()
        );

        String normalizedName = normalizeRequired(request.getName());
        String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());
        if (borrowerRepository.existsByOwnerUser_IdAndPhoneNumberAndActiveTrueAndIdNot(
                currentUser.getId(),
                normalizedPhoneNumber,
                borrowerId
        )) {
            throw new DuplicateResourceException("Borrower with this phone number already exists");
        }

        borrower.updateDetails(
                normalizedName,
                normalizedPhoneNumber
        );

        try {
            Borrower savedBorrower = borrowerRepository.save(borrower);
            return BorrowerResponse.from(savedBorrower);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("Borrower with this phone number already exists");
        }
    }

    @Transactional
    public void deleteBorrower(UUID borrowerId) {
        AppUser currentUser = currentUserService.getCurrentUser();
        Borrower borrower = getActiveBorrowerForUser(
                borrowerId,
                currentUser.getId()
        );

        borrower.deactivate();

        borrowerRepository.save(borrower);
    }

    private Borrower getActiveBorrowerForUser(UUID borrowerId, UUID ownerUserId) {
        return borrowerRepository.findByIdAndOwnerUser_IdAndActiveTrue(borrowerId, ownerUserId).orElseThrow(() -> new ResourceNotFoundException("Borrower not found"));
    }

    private String normalizeRequired(String value) {
        return value.trim();
    }

    private String normalizePhoneNumber(String phoneNumber) {
        return phoneNumber.trim();
    }
}