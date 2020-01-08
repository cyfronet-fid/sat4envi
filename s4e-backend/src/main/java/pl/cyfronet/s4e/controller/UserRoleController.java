package pl.cyfronet.s4e.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.cyfronet.s4e.controller.request.CreateUserRoleRequest;
import pl.cyfronet.s4e.controller.request.DeleteUserRoleRequest;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.service.UserRoleService;

import javax.validation.Valid;

import static pl.cyfronet.s4e.Constants.API_PREFIX_V1;

@RestController
@RequestMapping(API_PREFIX_V1)
@RequiredArgsConstructor
@Tag(name = "user-role", description = "The User Role API")
@PreAuthorize("isAuthenticated()")
public class UserRoleController {
    private final UserRoleService userRoleService;

    @Operation(summary = "Add a role to user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If user role was added"),
            @ApiResponse(responseCode = "400", description = "User role was not added"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to add a role"),
            @ApiResponse(responseCode = "404", description = "Role, group or user not found")
    })
    @PostMapping("/user-role")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<?> add(@RequestBody @Valid CreateUserRoleRequest request) throws NotFoundException {
        userRoleService.addRole(request.getRole(), request.getEmail(), request.getInstitutionSlug(), request.getGroupSlug());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Remove a role from user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "If user role was removed"),
            @ApiResponse(responseCode = "400", description = "User role was not removed"),
            @ApiResponse(responseCode = "403", description = "Forbidden: Don't have permission to remove a role"),
            @ApiResponse(responseCode = "404", description = "Role, group or user not found")
    })
    @DeleteMapping("/user-role")
    @PreAuthorize("isAdmin()")
    public ResponseEntity<?> remove(@RequestBody @Valid DeleteUserRoleRequest request) throws NotFoundException {
        userRoleService.removeRole(request.getRole(), request.getEmail(), request.getInstitutionSlug(), request.getGroupSlug());
        return ResponseEntity.ok().build();
    }
}
