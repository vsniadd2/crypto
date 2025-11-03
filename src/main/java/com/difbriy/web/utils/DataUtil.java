package com.difbriy.web.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class DataUtil {
    private final Clock clock;

    public Instant now() {
        return clock.instant();
    }
}
