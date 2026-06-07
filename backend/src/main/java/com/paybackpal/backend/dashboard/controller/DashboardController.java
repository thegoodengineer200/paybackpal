package com.paybackpal.backend.dashboard.controller;

import com.paybackpal.backend.dashboard.dto.BorrowerSummaryResponse;
import com.paybackpal.backend.dashboard.dto.CardSummaryResponse;
import com.paybackpal.backend.dashboard.dto.DashboardSummaryResponse;
import com.paybackpal.backend.dashboard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard/summary")
    public DashboardSummaryResponse getDashboardSummary() {
        return dashboardService.getDashboardSummary();
    }

    @GetMapping("/cards/{cardId}/summary")
    public CardSummaryResponse getCardSummary(@PathVariable UUID cardId) {
        return dashboardService.getCardSummary(cardId);
    }

    @GetMapping("/borrowers/{borrowerId}/summary")
    public BorrowerSummaryResponse getBorrowerSummary(@PathVariable UUID borrowerId) {
        return dashboardService.getBorrowerSummary(borrowerId);
    }
}