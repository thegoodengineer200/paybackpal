package com.paybackpal.backend.borrower.controller;

import com.paybackpal.backend.borrower.dto.BorrowerResponse;
import com.paybackpal.backend.borrower.dto.CreateBorrowerRequest;
import com.paybackpal.backend.borrower.dto.UpdateBorrowerRequest;
import com.paybackpal.backend.borrower.service.BorrowerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    public BorrowerController(BorrowerService borrowerService) {
        this.borrowerService = borrowerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowerResponse createBorrower(
            @Valid @RequestBody CreateBorrowerRequest request
    ) {
        return borrowerService.createBorrower(request);
    }

    @GetMapping
    public List<BorrowerResponse> getCurrentUserBorrowers() {
        return borrowerService.getCurrentUserBorrowers();
    }

    @GetMapping("/{borrowerId}")
    public BorrowerResponse getBorrower(@PathVariable UUID borrowerId) {
        return borrowerService.getBorrower(borrowerId);
    }

    @PutMapping("/{borrowerId}")
    public BorrowerResponse updateBorrower(
            @PathVariable UUID borrowerId,
            @Valid @RequestBody UpdateBorrowerRequest request
    ) {
        return borrowerService.updateBorrower(borrowerId, request);
    }

    @DeleteMapping("/{borrowerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBorrower(@PathVariable UUID borrowerId) {
        borrowerService.deleteBorrower(borrowerId);
    }
}