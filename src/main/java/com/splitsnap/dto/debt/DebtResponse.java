package com.splitsnap.dto.debt;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DebtResponse {
    private String id;
    private UserDebtInfoDTO fromUser;
    private UserDebtInfoDTO toUser;
    private BigDecimal amount;
    private String status;
    private String expenseId;
    private LocalDateTime paidAt;
    private String paidWith;

    public DebtResponse() {
    }

    // ── GETTERS Y SETTERS ─────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public UserDebtInfoDTO getFromUser() { return fromUser; }
    public void setFromUser(UserDebtInfoDTO fromUser) { this.fromUser = fromUser; }

    public UserDebtInfoDTO getToUser() { return toUser; }
    public void setToUser(UserDebtInfoDTO toUser) { this.toUser = toUser; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getExpenseId() { return expenseId; }
    public void setExpenseId(String expenseId) { this.expenseId = expenseId; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public String getPaidWith() { return paidWith; }
    public void setPaidWith(String paidWith) { this.paidWith = paidWith; }
}