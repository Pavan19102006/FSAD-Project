package com.loanmanagement.dto.response;

import java.math.BigDecimal;
import java.util.Map;

public class DashboardResponse {
    private long totalUsers;
    private long totalLoans;
    private long activeLoans;
    private long pendingApplications;
    private BigDecimal totalLoanAmount;
    private BigDecimal totalPaidAmount;
    private BigDecimal overdueAmount;
    private Map<String, Long> loansByStatus;
    private Map<String, Long> usersByRole;

    public DashboardResponse() {
    }

    // Getters and Setters
    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getTotalLoans() {
        return totalLoans;
    }

    public void setTotalLoans(long totalLoans) {
        this.totalLoans = totalLoans;
    }

    public long getActiveLoans() {
        return activeLoans;
    }

    public void setActiveLoans(long activeLoans) {
        this.activeLoans = activeLoans;
    }

    public long getPendingApplications() {
        return pendingApplications;
    }

    public void setPendingApplications(long pendingApplications) {
        this.pendingApplications = pendingApplications;
    }

    public BigDecimal getTotalLoanAmount() {
        return totalLoanAmount;
    }

    public void setTotalLoanAmount(BigDecimal totalLoanAmount) {
        this.totalLoanAmount = totalLoanAmount;
    }

    public BigDecimal getTotalPaidAmount() {
        return totalPaidAmount;
    }

    public void setTotalPaidAmount(BigDecimal totalPaidAmount) {
        this.totalPaidAmount = totalPaidAmount;
    }

    public BigDecimal getOverdueAmount() {
        return overdueAmount;
    }

    public void setOverdueAmount(BigDecimal overdueAmount) {
        this.overdueAmount = overdueAmount;
    }

    public Map<String, Long> getLoansByStatus() {
        return loansByStatus;
    }

    public void setLoansByStatus(Map<String, Long> loansByStatus) {
        this.loansByStatus = loansByStatus;
    }

    public Map<String, Long> getUsersByRole() {
        return usersByRole;
    }

    public void setUsersByRole(Map<String, Long> usersByRole) {
        this.usersByRole = usersByRole;
    }

    // Builder
    public static DashboardResponseBuilder builder() {
        return new DashboardResponseBuilder();
    }

    public static class DashboardResponseBuilder {
        private long totalUsers;
        private long totalLoans;
        private long activeLoans;
        private long pendingApplications;
        private BigDecimal totalLoanAmount;
        private BigDecimal totalPaidAmount;
        private BigDecimal overdueAmount;
        private Map<String, Long> loansByStatus;
        private Map<String, Long> usersByRole;

        public DashboardResponseBuilder totalUsers(long totalUsers) {
            this.totalUsers = totalUsers;
            return this;
        }

        public DashboardResponseBuilder totalLoans(long totalLoans) {
            this.totalLoans = totalLoans;
            return this;
        }

        public DashboardResponseBuilder activeLoans(long activeLoans) {
            this.activeLoans = activeLoans;
            return this;
        }

        public DashboardResponseBuilder pendingApplications(long pendingApplications) {
            this.pendingApplications = pendingApplications;
            return this;
        }

        public DashboardResponseBuilder totalLoanAmount(BigDecimal totalLoanAmount) {
            this.totalLoanAmount = totalLoanAmount;
            return this;
        }

        public DashboardResponseBuilder totalPaidAmount(BigDecimal totalPaidAmount) {
            this.totalPaidAmount = totalPaidAmount;
            return this;
        }

        public DashboardResponseBuilder overdueAmount(BigDecimal overdueAmount) {
            this.overdueAmount = overdueAmount;
            return this;
        }

        public DashboardResponseBuilder loansByStatus(Map<String, Long> loansByStatus) {
            this.loansByStatus = loansByStatus;
            return this;
        }

        public DashboardResponseBuilder usersByRole(Map<String, Long> usersByRole) {
            this.usersByRole = usersByRole;
            return this;
        }

        public DashboardResponse build() {
            DashboardResponse r = new DashboardResponse();
            r.totalUsers = this.totalUsers;
            r.totalLoans = this.totalLoans;
            r.activeLoans = this.activeLoans;
            r.pendingApplications = this.pendingApplications;
            r.totalLoanAmount = this.totalLoanAmount;
            r.totalPaidAmount = this.totalPaidAmount;
            r.overdueAmount = this.overdueAmount;
            r.loansByStatus = this.loansByStatus;
            r.usersByRole = this.usersByRole;
            return r;
        }
    }
}
