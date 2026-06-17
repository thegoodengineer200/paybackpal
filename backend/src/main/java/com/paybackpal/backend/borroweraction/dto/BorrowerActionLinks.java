package com.paybackpal.backend.borroweraction.dto;

public class BorrowerActionLinks {

    private final String reportPaidUrl;
    private final String remindMeLaterUrl;

    public BorrowerActionLinks(
            String reportPaidUrl,
            String remindMeLaterUrl
    ) {
        this.reportPaidUrl = reportPaidUrl;
        this.remindMeLaterUrl = remindMeLaterUrl;
    }

    public String getReportPaidUrl() {
        return reportPaidUrl;
    }

    public String getRemindMeLaterUrl() {
        return remindMeLaterUrl;
    }
}