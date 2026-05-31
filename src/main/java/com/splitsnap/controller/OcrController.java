package com.splitsnap.controller;

import com.splitsnap.dto.expense.OcrResponse;
import com.splitsnap.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR", description = "Escaneo de recibos con Google Cloud Vision")
@SecurityRequirement(name = "bearerAuth")
public class OcrController {

    private final ExpenseService expenseService;

    @PostMapping(value = "/scan", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Escanear un recibo y extraer ítems (HU-4.5)",
        description = "Procesa la imagen del recibo con Google Cloud Vision y devuelve descripción, monto y texto extraído"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OCR procesado correctamente"),
        @ApiResponse(responseCode = "400", description = "Archivo vacío o inválido"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "500", description = "Error de Google Cloud Vision")
    })
    public ResponseEntity<OcrResponse> scanReceipt(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(expenseService.processReceiptOcr(file));
    }
}
