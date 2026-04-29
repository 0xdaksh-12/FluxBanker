package com.fluxbanker.api.controller;

import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.dto.response.UserResponse;
import com.fluxbanker.api.service.AdminService;
import com.fluxbanker.api.types.PaginatedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Controller for admin-only operations. */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

  private final AdminService adminService;

  @GetMapping("/users")
  public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable) {
    return adminService.getAllUsers(pageable);
  }

  @GetMapping("/transactions")
  public PaginatedResponse<TransactionDto> getAllTransactions(Pageable pageable) {
    return adminService.getAllTransactions(pageable);
  }
}
