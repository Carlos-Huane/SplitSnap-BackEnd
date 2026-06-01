package com.splitsnap.service;

import com.google.protobuf.ByteString;
import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseDetailResponse;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.dto.expense.OcrResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.exception.EntityNotFoundException;
import com.splitsnap.model.Expense;
import com.splitsnap.model.ExpenseSplit;
import com.splitsnap.model.Debt;
import com.splitsnap.model.Group;
import com.splitsnap.model.User;
import com.splitsnap.repository.ExpenseRepository;
import com.splitsnap.repository.ExpenseSplitRepository;
import com.splitsnap.repository.GroupMemberRepository;
import com.splitsnap.repository.DebtRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DebtRepository debtRepository;
    private final GroupService groupService;
    private final UserService userService;

    @Transactional
    public ExpenseResponse createExpense(UUID groupId, CreateExpenseRequest request, User authenticatedUser) {

        Group group = groupService.findById(groupId);

        assertIsMember(groupId, authenticatedUser.getId());

        User payer = (request.getPaidBy() != null)
                ? userService.findById(request.getPaidBy())
                : authenticatedUser;

        if (!payer.getId().equals(authenticatedUser.getId())
                && !groupMemberRepository.existsByGroupIdAndUserId(groupId, payer.getId())) {
            throw new BusinessException("El usuario que pagó no es miembro del grupo.");
        }

        double totalSplitsSum = request.getSplitBetween().stream()
                .mapToDouble(CreateExpenseRequest.SplitEntry::getAmount)
                .sum();
        if (Math.abs(totalSplitsSum - request.getAmount()) > 0.01) {
            throw new BusinessException("La suma de los montos individuales (" + totalSplitsSum
                    + ") no coincide con el monto total del gasto (" + request.getAmount() + ").");
        }

        Expense expense = Expense.builder()
                .id(UUID.randomUUID().toString())
                .description(request.getDescription())
                .amount(BigDecimal.valueOf(request.getAmount()))
                .group(group)
                .paidBy(payer)
                .createdBy(authenticatedUser)
                .expenseDate(request.getDate() != null ? request.getDate() : LocalDate.now())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        for (CreateExpenseRequest.SplitEntry entry : request.getSplitBetween()) {
            User member = userService.findById(entry.getUserId());

            if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, member.getId())) {
                throw new BusinessException("El usuario " + member.getName() + " no es miembro del grupo.");
            }

            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(member)
                    .amount(entry.getAmount())
                    .build();
            expenseSplitRepository.save(split);

            if (!member.getId().equals(payer.getId())) {
                Debt debt = new Debt();
                debt.setId(UUID.randomUUID().toString());
                debt.setGroup(group);
                debt.setExpenseId(savedExpense.getId());
                debt.setFromUser(member);
                debt.setToUser(payer);
                debt.setAmount(BigDecimal.valueOf(entry.getAmount()));
                debt.setStatus("PENDING");
                debtRepository.save(debt);
            }
        }

        return ExpenseResponse.from(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getExpensesByGroup(UUID groupId, User authenticatedUser) {
        groupService.findById(groupId);
        assertIsMember(groupId, authenticatedUser.getId());

        return expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId).stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ExpenseDetailResponse getExpenseDetails(UUID groupId, UUID expenseId, User authenticatedUser) {
        groupService.findById(groupId);
        assertIsMember(groupId, authenticatedUser.getId());

        Expense expense = expenseRepository.findById(expenseId.toString())
                .orElseThrow(() -> new EntityNotFoundException("El gasto solicitado no existe."));

        if (expense.getGroup() == null || !expense.getGroup().getId().equals(groupId)) {
            throw new EntityNotFoundException("El gasto no pertenece a este grupo.");
        }

        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expenseId.toString());

        List<ExpenseDetailResponse.SplitUserDetail> splitDetails = splits.stream()
                .map(split -> ExpenseDetailResponse.SplitUserDetail.builder()
                        .userId(split.getUser().getId())
                        .userName(split.getUser().getName())
                        .amount(split.getAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseDetailResponse.builder()
                .id(UUID.fromString(expense.getId()))
                .description(expense.getDescription())
                .amount(expense.getAmountAsDouble())
                .paidBy(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
                .paidByName(expense.getPaidBy() != null ? expense.getPaidBy().getName() : "Usuario Desconocido")
                .expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate()
                        : (expense.getCreatedAt() != null ? expense.getCreatedAt().toLocalDate() : LocalDate.now()))
                .splits(splitDetails)
                .build();
    }

    public OcrResponse processReceiptOcr(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("El archivo del recibo no puede estar vacío.");
        }

        try {
            byte[] imgBytes = file.getBytes();
            ByteString imgByteString = ByteString.copyFrom(imgBytes);
            Image img = Image.newBuilder().setContent(imgByteString).build();

            Feature feat = Feature.newBuilder().setType(Type.TEXT_DETECTION).build();
            AnnotateImageRequest request = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat)
                    .setImage(img)
                    .build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            String rawText = "";

            try (ImageAnnotatorClient client = createVisionClient()) {
                List<AnnotateImageResponse> responses = client.batchAnnotateImages(requests).getResponsesList();
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        throw new BusinessException("Error de Google Vision: " + res.getError().getMessage());
                    }
                    rawText = res.getTextAnnotationsList().get(0).getDescription();
                }
            }

            // Boletas SUNAT peruanas usan: "PRECIO VENTA", "VALOR VENTA",
            // "TOTAL", "TOTAL A PAGAR", "IMPORTE TOTAL", "MONTO TOTAL",
            // "SUBTOTAL", "NETO", "PAGO", "IMPORTE". Cubrimos todas las
            // variantes y aceptamos espacios/S/ entre la palabra y el monto.
            //
            // El monto se busca con LOOKAHEAD (?= ... ) para que el matcher
            // solo consuma la keyword. Sin lookahead, un match de
            // "OP. GRAVADAS ... 99.50" se comeria el texto que contiene
            // "IMPORTE TOTAL" en medio y nunca lo encontrariamos como match
            // separado de mayor prioridad.
            //
            // Ventana de 250 caracteres entre keyword y monto porque Google
            // Vision suele leer columnas izquierda/derecha de boletas como
            // bloques separados: las palabras "IMPORTE TOTAL" aparecen
            // agrupadas con OP GRAVADAS / IGV, y los montos numericos vienen
            // varias lineas despues como bloque aparte.
            Pattern pattern = Pattern.compile(
                    "(?i)\\b(precio\\s+venta|valor\\s+venta|gran\\s+total|total(?:\\s+a\\s+pagar)?|importe(?:\\s+total)?|monto\\s+total|por\\s+pagar|op\\.?\\s*gravadas?|subtotal|neto|pago)\\b(?=[\\s\\S]{0,250}?(\\d{1,7}[.,]\\d{2}))"
            );
            // Prioridad: "precio venta" / "total a pagar" cierran el ticket SUNAT.
            // "op gravadas" es subtotal sin IGV (prioridad muy baja).
            String[] priority = { "precio venta", "total a pagar", "por pagar", "gran total",
                                  "total", "monto total", "importe total", "valor venta",
                                  "importe", "neto", "pago", "subtotal", "op gravadas",
                                  "op. gravadas", "op gravada", "op. gravada" };
            Double detectedAmount = 0.0;
            int bestPriority = Integer.MAX_VALUE;
            String matchedKeyword = null;
            Matcher matcher = pattern.matcher(rawText);
            while (matcher.find()) {
                try {
                    double val = Double.parseDouble(matcher.group(2).replace(",", "."));
                    String kw = matcher.group(1).toLowerCase().replaceAll("\\s+", " ").trim();
                    int prio = priority.length;
                    for (int i = 0; i < priority.length; i++) {
                        if (kw.equals(priority[i])) { prio = i; break; }
                    }
                    // Mejor prioridad gana. A igual prioridad nos quedamos con el mayor monto
                    // (algunos tickets repiten el total varias veces y queremos el numero real).
                    if (prio < bestPriority || (prio == bestPriority && val > detectedAmount)) {
                        bestPriority = prio;
                        detectedAmount = val;
                        matchedKeyword = kw;
                    }
                } catch (NumberFormatException ignored) {
                    // saltamos matches no parseables
                }
            }

            // extractedItems: devolvemos las lineas completas del recibo (sin truncar)
            // para que el front pueda mostrar el texto OCR completo en el detalle.
            List<String> extractedLines = Arrays.stream(rawText.split("\\r?\\n"))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            String detectedDescription = extractedLines.isEmpty()
                    ? "Gasto Escaneado OCR"
                    : extractedLines.get(0);

            return OcrResponse.builder()
                    .description("Escaneo: " + detectedDescription)
                    .detectedAmount(detectedAmount)
                    .confidenceScore(detectedAmount > 0
                            ? (matchedKeyword != null ? matchedKeyword : "extracted")
                            : "fallback")
                    .extractedItems(extractedLines)
                    .build();

        } catch (Exception e) {
            throw new BusinessException("Error al procesar el OCR: " + e.getMessage());
        }
    }

    private void assertIsMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("No eres miembro de este grupo.");
        }
    }

    /**
     * Construye el cliente de Google Cloud Vision soportando dos formas de credenciales:
     *
     *  1) Env var GOOGLE_APPLICATION_CREDENTIALS_JSON con el contenido completo del JSON
     *     (recomendado para deploy en Railway/Render donde no hay file system persistente).
     *  2) ADC clásico: env var GOOGLE_APPLICATION_CREDENTIALS apuntando a la ruta del archivo
     *     (recomendado para desarrollo local).
     */
    private ImageAnnotatorClient createVisionClient() throws IOException {
        String jsonContent = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");
        if (jsonContent != null && !jsonContent.isBlank()) {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(jsonContent.getBytes(StandardCharsets.UTF_8))) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(stream);
                ImageAnnotatorSettings settings = ImageAnnotatorSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build();
                return ImageAnnotatorClient.create(settings);
            }
        }
        return ImageAnnotatorClient.create();
    }
}
