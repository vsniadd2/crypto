package com.difbriy.web.controller.crypto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.difbriy.web.service.crypto.CryptoService;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/api")
public class CurrencyController {

    CryptoService cryptoService;
    ObjectMapper objectMapper;


    @GetMapping("/currency")
    public ResponseEntity<List<Map<String, Object>>> getCurrencyData() throws IOException {
        return cryptoService.fetchDataFromCoinGecko();

    }
}