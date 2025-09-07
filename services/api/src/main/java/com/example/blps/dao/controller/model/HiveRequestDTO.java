package com.example.blps.dao.controller.model;

import java.util.List;

public record HiveRequestDTO(
        List<HiveInput> input
) {
    public record HiveInput(
             String text
    ) {}
}