package com.splitsnap.controller;

import com.splitsnap.dto.group.AddMemberRequest;
import com.splitsnap.dto.group.CreateGroupRequest;
import com.splitsnap.dto.group.GroupResponse;
import com.splitsnap.model.User;
import com.splitsnap.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Groups", description = "Gestión de grupos")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Crear grupo")
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar mis grupos")
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.getMyGroups(user.getId()));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Ver detalle de un grupo")
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.getGroupById(groupId, user.getId()));
    }

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Agregar miembro al grupo")
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.addMember(groupId, request, user.getId()));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Eliminar miembro del grupo")
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user) {
        groupService.removeMember(groupId, userId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
