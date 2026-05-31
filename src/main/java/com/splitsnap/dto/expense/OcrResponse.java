package com.splitsnap.dto.expense;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class OcrResponse {
    private String description;
    private Double detectedAmount;
    private String confidenceScore; // Porcentaje de seguridad de la extracción (ej: "95%")
    private List<String> extractedItems; // Líneas o productos detectados opcionalmente
}