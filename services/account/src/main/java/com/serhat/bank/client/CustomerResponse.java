package com.serhat.bank.client;

import lombok.NoArgsConstructor;


public record CustomerResponse(
        Integer id,
        String name,
        String surname,
        String email,
        String personalId

) {
}
