package com.serhat.bank.client;

import lombok.NoArgsConstructor;

import java.io.Serializable;


public record CustomerResponse(
        Integer id,
        String name,
        String surname,
        String email,
        String personalId

) implements Serializable {
    private static final long serialVersionUID = 1L;

}
