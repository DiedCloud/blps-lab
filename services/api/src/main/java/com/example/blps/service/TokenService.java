package com.example.blps.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class TokenService {
    private final static Map<Long, String> userTokens = new HashMap<>();

    public static String getUserToken(Long userId) {
        return userTokens.getOrDefault(userId, null);
    }

    public static void putUserToken(Long userId, String token) {
        userTokens.put(userId, token);
    }
}