package com.fluxbanker.api.service;

import com.fluxbanker.api.dto.TransactionDto;
import com.fluxbanker.api.dto.response.UserResponse;
import com.fluxbanker.api.repository.TransactionRepository;
import com.fluxbanker.api.repository.UserRepository;
import com.fluxbanker.api.types.PaginatedResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserRepository userRepository;
  private final TransactionRepository transactionRepository;

  public PaginatedResponse<UserResponse> getAllUsers(Pageable pageable) {
    org.springframework.data.domain.Page<com.fluxbanker.api.entity.User> page = userRepository.findAll(pageable);
    List<UserResponse> content = page.getContent().stream()
            .map(UserResponse::fromEntity)
            .toList();
            
    return new PaginatedResponse<>(
        content,
        page.getTotalPages(),
        page.getTotalElements(),
        page.getNumber(),
        page.getSize());
  }

  public PaginatedResponse<TransactionDto> getAllTransactions(Pageable pageable) {
    org.springframework.data.domain.Page<com.fluxbanker.api.entity.Transaction> page = transactionRepository.findAll(pageable);
    List<TransactionDto> content = page.getContent().stream()
            .map(
                tx ->
                    TransactionDto.builder()
                        .id(tx.getId())
                        .accountId(tx.getAccount().getId())
                        .amount(tx.getAmount())
                        .type(tx.getType())
                        .status(tx.getStatus())
                        .category(tx.getCategory())
                        .counterpartyName(tx.getCounterpartyName())
                        .timestamp(tx.getTimestamp())
                        .build())
            .toList();

    return new PaginatedResponse<>(
        content,
        page.getTotalPages(),
        page.getTotalElements(),
        page.getNumber(),
        page.getSize());
  }
}
