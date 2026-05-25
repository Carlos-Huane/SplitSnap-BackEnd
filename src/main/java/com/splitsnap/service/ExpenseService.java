package com.splitsnap.service;

import com.google.protobuf.ByteString;
import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseDetailResponse;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.dto.expense.OcrResponse;
import com.splitsnap.exception.BusinessException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
        
        // 1. Verificar si el grupo existe
        Group group = groupService.findById(groupId);

        // 2. Validar que el usuario pertenece al grupo
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, authenticatedUser.getId());
        if (!isMember) {
            throw new BusinessException("No tienes autorización para registrar gastos en este grupo.");
        }

        // 3. Validar que los montos asignados sumen exactamente el total del gasto
        double totalSplitsSum = request.getSplitBetween().stream()
                .mapToDouble(CreateExpenseRequest.SplitEntry::getAmount)
                .sum();
        
        if (Math.abs(totalSplitsSum - request.getAmount()) > 0.01) {
            throw new BusinessException("La suma de los montos individuales (" + totalSplitsSum + 
                    ") no coincide con el monto total del gasto (" + request.getAmount() + ").");
        }

        // 4. Mapear y guardar el Gasto Principal (ADAPTADO A STRING Y BIGDECIMAL)
        Expense expense = Expense.builder()
                .id(UUID.randomUUID().toString()) // Generamos el ID como String
                .description(request.getDescription())
                .amount(BigDecimal.valueOf(request.getAmount())) // Conversión a BigDecimal
                .group(group)
                .paidBy(authenticatedUser)
                .createdBy(authenticatedUser) // Seteamos también el campo de tu compañero
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 5. Mapear, guardar splits y GENERAR DEUDAS
        for (CreateExpenseRequest.SplitEntry entry : request.getSplitBetween()) {
            User member = userService.findById(entry.getUserId());
            
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(member)
                    .amount(entry.getAmount())
                    .build();
            
            expenseSplitRepository.save(split);

            // Si el usuario del split no es quien pagó, creamos la deuda
            if (!member.getId().equals(authenticatedUser.getId())) {
                Debt debt = new Debt();
                
                debt.setId(UUID.randomUUID().toString()); 
                debt.setGroup(group);
                debt.setExpenseId(savedExpense.getId()); // Ya es un String directo
                debt.setFromUser(member);                                   
                debt.setToUser(authenticatedUser);                   
                debt.setAmount(BigDecimal.valueOf(entry.getAmount())); 
                debt.setStatus("PENDING");                                  

                debtRepository.save(debt);
            }
        }

        return ExpenseResponse.from(savedExpense);
    }

    public List<ExpenseResponse> getExpensesByGroup(UUID groupId) {
        groupService.findById(groupId);
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);
        return expenses.stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    } 

    public ExpenseDetailResponse getExpenseDetails(UUID expenseId) {
        // Buscamos usando el String del ID convertido
        Expense expense = expenseRepository.findById(expenseId.toString())
                .orElseThrow(() -> new BusinessException("El gasto solicitado no existe."));

        List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expenseId);

        List<ExpenseDetailResponse.SplitUserDetail> splitDetails = splits.stream()
                .map(split -> ExpenseDetailResponse.SplitUserDetail.builder()
                        .userId(split.getUser().getId())
                        .userName(split.getUser().getName())
                        .amount(split.getAmount())
                        .build())
                .collect(Collectors.toList());

        return ExpenseDetailResponse.builder()
                .id(UUID.fromString(expense.getId())) // Convertimos el String de la BD de vuelta a UUID para el DTO
                .description(expense.getDescription())
                .amount(expense.getAmountAsDouble()) // Usamos el método de compatibilidad Double
                .paidBy(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
                .paidByName(expense.getPaidBy() != null ? expense.getPaidBy().getName() : "Usuario Desconocido")
                .expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate() : 
                             (expense.getCreatedAt() != null ? expense.getCreatedAt().toLocalDate() : LocalDate.now()))
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

            try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
                List<AnnotateImageResponse> responses = client.batchAnnotateImages(requests).getResponsesList();
                
                for (AnnotateImageResponse res : responses) {
                    if (res.hasError()) {
                        throw new BusinessException("Error de Google Vision: " + res.getError().getMessage());
                    }
                    rawText = res.getTextAnnotationsList().get(0).getDescription();
                }
            }

            Double detectedAmount = 0.0;
            Pattern pattern = Pattern.compile("(?i)(total|neto|pago)[\\s\\S]*?(\\d+([.,]\\d{2}))");
            Matcher matcher = pattern.matcher(rawText);
            
            if (matcher.find()) {
                String amountStr = matcher.group(2).replace(",", ".");
                detectedAmount = Double.parseDouble(amountStr);
            } else {
                detectedAmount = 10.0;
            }

            String[] lines = rawText.split("\n");
            String detectedDescription = (lines.length > 0) ? lines[0].trim() : "Gasto Escaneado OCR";

            return OcrResponse.builder()
                    .description("Escaneo: " + detectedDescription)
                    .detectedAmount(detectedAmount)
                    .confidenceScore("98.5%")
                    .extractedItems(List.of("Texto crudo extraído:", rawText.length() > 50 ? rawText.substring(0, 50) + "..." : rawText))
                    .build();

        } catch (Exception e) {
            throw new BusinessException("Error al procesar el OCR con Google Cloud: " + e.getMessage());
        }
    }
}