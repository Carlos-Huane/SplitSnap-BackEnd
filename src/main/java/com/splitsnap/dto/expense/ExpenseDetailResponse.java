package com.splitsnap.dto.expense;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
public class ExpenseDetailResponse {
    private UUID id;
    private String description;
    private Double amount;
    private UUID paidBy;
    private String paidByName;
    private LocalDate expenseDate;
    private List<SplitUserDetail> splits;

    @Getter
    @Builder
    public static class SplitUserDetail {
        private UUID userId;
        private String userName;
        private Double amount;
    }
}