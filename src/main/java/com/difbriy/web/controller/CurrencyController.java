package com.difbriy.web.controller;

import java.io.IOException;

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
    public ResponseEntity<?> getCurrencyData() throws IOException {
        return cryptoService.fetchDataFromCoinGecko();

    }

//    @GetMapping("/currency/{id}")
//    public ResponseEntity<?> getCurrentById(@PathVariable Long id){
//        return cryptoService.fetchSingleCoin(id);
//    }
}