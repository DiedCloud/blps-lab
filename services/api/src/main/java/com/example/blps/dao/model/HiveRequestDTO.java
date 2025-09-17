package com.example.blps.dao.model;

import java.util.List;

public record HiveRequestDTO(
        List<HiveInput> input
) {
    public record HiveInput(
             String text
    ) {}
}