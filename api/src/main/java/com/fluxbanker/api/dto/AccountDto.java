package com.fluxbanker.api.dto;

import com.fluxbanker.api.entity.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {

    private UUID id;
    private String name;
    private String mask;
    private BigDecimal currentBalance;
    private BigDecimal availableBalance;
    private Account.Type type;
    private Account.Subtype subtype;
}
