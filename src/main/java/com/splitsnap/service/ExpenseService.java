package com.splitsnap.service;

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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List; // <-- ¡AQUÍ ESTÁ LA IMPORTACIÓN QUE FALTABA!
import java.util.UUID;
import java.util.stream.Collectors;

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

        // 2. Regla de Negocio: Validar que el usuario que registra pertenece al grupo
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, authenticatedUser.getId());
        if (!isMember) {
            throw new BusinessException("No tienes autorización para registrar gastos en este grupo.");
        }

        // 3. Regla de Negocio: Validar que los montos asignados sumen exactamente el total del gasto
        double totalSplitsSum = request.getSplitBetween().stream()
                .mapToDouble(CreateExpenseRequest.SplitEntry::getAmount)
                .sum();
        
        if (Math.abs(totalSplitsSum - request.getAmount()) > 0.01) {
            throw new BusinessException("La suma de los montos individuales (" + totalSplitsSum + 
                    ") no coincide con el monto total del gasto (" + request.getAmount() + ").");
        }

        // 4. Mapear y guardar el Gasto Principal
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .group(group)
                .paidBy(authenticatedUser)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 5. Mapear, guardar splits y GENERAR DEUDAS (SCRUM-97)
        for (CreateExpenseRequest.SplitEntry entry : request.getSplitBetween()) {
            User member = userService.findById(entry.getUserId());
            
            // A. Guardamos el split tradicional
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(member)
                    .amount(entry.getAmount())
                    .build();
            
            expenseSplitRepository.save(split);

            // B. LÓGICA SCRUM-97: Si el usuario del split no es quien pagó, le creamos una deuda automática
            if (!member.getId().equals(authenticatedUser.getId())) {
                Debt debt = new Debt();
                
                debt.setId(UUID.randomUUID().toString()); 
                debt.setGroup(group);
                debt.setExpenseId(savedExpense.getId().toString()); 
                debt.setFromUser(member);                            
                debt.setToUser(authenticatedUser);                   
                debt.setAmount(BigDecimal.valueOf(entry.getAmount())); 
                debt.setStatus("PENDING");                           

                debtRepository.save(debt);
            }
        }

        return ExpenseResponse.from(savedExpense);
    } // <-- AQUÍ CIERRA CORRECTAMENTE createExpense

    // ------ LOGICA SCRUM-98: LISTAR GASTOS DE UN GRUPO ------
    public List<ExpenseResponse> getExpensesByGroup(UUID groupId) {
        // 1. Validar que el grupo exista utilizando tu servicio existente
        Group group = groupService.findById(groupId);

        // 2. Buscar todos los gastos asociados a este grupo, ordenados por fecha de creación descendente
        List<Expense> expenses = expenseRepository.findByGroupIdOrderByCreatedAtDesc(groupId);

        // 3. Transformar la lista de entidades a una lista de DTOs de respuesta
        return expenses.stream()
                .map(ExpenseResponse::from)
                .collect(Collectors.toList());
    } 


    public ExpenseDetailResponse getExpenseDetails(UUID expenseId) {
    // 1. Buscar el gasto o lanzar error si no existe
    Expense expense = expenseRepository.findById(expenseId)
            .orElseThrow(() -> new com.splitsnap.exception.BusinessException("El gasto solicitado no existe."));

    // 2. Buscar los desgloses (splits) asociados a este gasto
    List<ExpenseSplit> splits = expenseSplitRepository.findByExpenseId(expenseId);

    // 3. Mapear los desgloses a su sub-DTO correspondiente
    List<ExpenseDetailResponse.SplitUserDetail> splitDetails = splits.stream()
            .map(split -> ExpenseDetailResponse.SplitUserDetail.builder()
                    .userId(split.getUser().getId())
                    .userName(split.getUser().getName())
                    .amount(split.getAmount())
                    .build())
            .collect(Collectors.toList());

    // 4. Construir y retornar la respuesta detallada completa
    return ExpenseDetailResponse.builder()
            .id(expense.getId())
            .description(expense.getDescription())
            .amount(expense.getAmount())
            .paidBy(expense.getPaidBy() != null ? expense.getPaidBy().getId() : null)
            .paidByName(expense.getPaidBy() != null ? expense.getPaidBy().getName() : "Usuario Desconocido")
            .expenseDate(expense.getExpenseDate() != null ? expense.getExpenseDate() : 
                         (expense.getCreatedAt() != null ? expense.getCreatedAt().toLocalDate() : LocalDate.now()))
            .splits(splitDetails)
            .build();
}

    public OcrResponse processReceiptOcr(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new com.splitsnap.exception.BusinessException("El archivo del recibo no puede estar vacío.");
        }

        try {
            // En una fase avanzada, aquí integrarías la API de Google Vision o AWS Textract:
            // byte[] imgBytes = file.getBytes();
            // OcrResult result = visionClient.analyze(imgBytes);
            
            // Simulación profesional de extracción OCR basada en el nombre del archivo para testing:
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename().toLowerCase() : "";
            String detectedDescription = "Gasto por Escaneo OCR";
            Double detectedAmount = 45.50; // Monto base por defecto si no se reconoce el patrón

            if (fileName.contains("starbucks") || fileName.contains("cafe")) {
                detectedDescription = "Consumo Starbucks / Cafetería";
                detectedAmount = 32.80;
            } else if (fileName.contains("plaza") || fileName.contains("vea") || fileName.contains("metro")) {
                detectedDescription = "Compras Supermercado";
                detectedAmount = 124.90;
            } else if (fileName.contains("tambo") || fileName.contains("oxxo")) {
                detectedDescription = "Consumo Tambo / Tienda";
                detectedAmount = 18.50;
            }

            return OcrResponse.builder()
                    .description(detectedDescription)
                    .detectedAmount(detectedAmount)
                    .confidenceScore("94.8%")
                    .extractedItems(List.of("ITEM 01 - TOTAL PROCESADO", "IGV INCLUIDO"))
                    .build();

        } catch (Exception e) {
            throw new com.splitsnap.exception.BusinessException("Error al procesar el escaneo del recibo: " + e.getMessage());
        }
    }
}