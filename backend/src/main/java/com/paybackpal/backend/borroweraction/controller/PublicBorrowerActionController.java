package com.paybackpal.backend.borroweraction.controller;

import com.paybackpal.backend.borroweraction.dto.BorrowerActionResponse;
import com.paybackpal.backend.borroweraction.service.PublicBorrowerActionService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/borrower-actions")
public class PublicBorrowerActionController {

    private final PublicBorrowerActionService publicBorrowerActionService;

    public PublicBorrowerActionController(
            PublicBorrowerActionService publicBorrowerActionService
    ) {
        this.publicBorrowerActionService = publicBorrowerActionService;
    }

    @PostMapping("/{token}/report-paid")
    public BorrowerActionResponse reportPaid(
            @PathVariable String token
    ) {
        return publicBorrowerActionService.reportPaid(token);
    }

    @PostMapping("/{token}/remind-me-later")
    public BorrowerActionResponse remindMeLater(@PathVariable String token) {
        return publicBorrowerActionService.remindMeLater(token);
    }
}