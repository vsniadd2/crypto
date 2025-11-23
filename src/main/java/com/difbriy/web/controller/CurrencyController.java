package com.difbriy.web.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.difbriy.web.service.crypto.CryptoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class CurrencyController {

    private final CryptoService cryptoService;
    private final ObjectMapper objectMapper;


    @GetMapping("/currency")
    public ResponseEntity<List<Map<String, Object>>> getCurrencyData() throws IOException {
        return cryptoService.fetchDataFromCoinGecko();

    }
}