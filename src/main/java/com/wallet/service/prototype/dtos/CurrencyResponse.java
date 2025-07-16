package com.wallet.service.prototype.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CurrencyResponse {
    private String code;

    private String name;

    private String symbol;
}
