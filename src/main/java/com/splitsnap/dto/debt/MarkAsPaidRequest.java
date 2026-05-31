package com.splitsnap.dto.debt;

import jakarta.validation.constraints.NotBlank;

public class MarkAsPaidRequest {
    @NotBlank(message = "El método de pago es obligatorio")
    private String paidWith; // Ejemplo: "yape", "paypal", "efectivo"

    // Getters y Setters
    public String getPaidWith() { return paidWith; }
    public void setPaidWith(String paidWith) { this.paidWith = paidWith; }
}