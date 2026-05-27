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
/**
 * @apiDefine GroupsGroup Gestión de grupos compartidos.
 */
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    @Operation(summary = "Crear grupo")
    /**
     * @api {post} /api/groups Crear grupo
     * @apiName CreateGroup
     * @apiGroup Groups
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} name Nombre del grupo.
     * @apiParam {String} [emoji] Emoji del grupo.
     * @apiParam {String[]} [memberIds] Miembros iniciales.
     */
    public ResponseEntity<GroupResponse> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request, user));
    }

    @GetMapping
    @Operation(summary = "Listar mis grupos")
    /**
     * @api {get} /api/groups Listar mis grupos
     * @apiName GetMyGroups
     * @apiGroup Groups
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     */
    public ResponseEntity<List<GroupResponse>> getMyGroups(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.getMyGroups(user.getId()));
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Ver detalle de un grupo")
    /**
     * @api {get} /api/groups/:groupId Ver detalle de un grupo
     * @apiName GetGroup
     * @apiGroup Groups
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} groupId ID del grupo.
     */
    public ResponseEntity<GroupResponse> getGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.getGroupById(groupId, user.getId()));
    }

    @PostMapping("/{groupId}/members")
    @Operation(summary = "Agregar miembro al grupo")
    /**
     * @api {post} /api/groups/:groupId/members Agregar miembro
     * @apiName AddMember
     * @apiGroup Groups
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} groupId ID del grupo.
     * @apiParam {String} userId ID del usuario a agregar.
     */
    public ResponseEntity<GroupResponse> addMember(
            @PathVariable UUID groupId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(groupService.addMember(groupId, request, user.getId()));
    }

    @DeleteMapping("/{groupId}/members/{userId}")
    @Operation(summary = "Eliminar miembro del grupo")
    /**
     * @api {delete} /api/groups/:groupId/members/:userId Eliminar miembro
     * @apiName RemoveMember
     * @apiGroup Groups
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} groupId ID del grupo.
     * @apiParam {String} userId ID del miembro a quitar.
     */
    public ResponseEntity<Void> removeMember(
            @PathVariable UUID groupId,
            @PathVariable UUID userId,
            @AuthenticationPrincipal User user) {
        groupService.removeMember(groupId, userId, user.getId());
        return ResponseEntity.noContent().build();
    }
}
