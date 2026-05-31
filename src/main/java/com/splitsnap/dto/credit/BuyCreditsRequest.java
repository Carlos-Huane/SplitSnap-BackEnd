package com.splitsnap.dto.credit;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BuyCreditsRequest {
    @NotNull(message = "El monto es obligatorio")
    @Min(value = 1, message = "El monto debe ser mayor a 0")
    private Integer amount;

    public Integer getAmount() { return amount; }
    public void setAmount(Integer amount) { this.amount = amount; }
}