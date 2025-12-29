package com.difbriy.web.exception;

import com.difbriy.web.exception.dto.*;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.IOException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import com.difbriy.web.exception.custom.InvalidResetTokenException;
import com.difbriy.web.exception.custom.AccessDeniedException;
import com.difbriy.web.exception.custom.AuthenticationEntryPointException;
import com.difbriy.web.exception.custom.LogoutHandlerException;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handlerIoException(IOException e) {
        String message = e.getMessage();
        log.error("Service unavailable due to IO exception: {}", message);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(IOExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerException(Exception e) {
        String message = e.getMessage();

        log.error("Service unavailable due to Exception: {}", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handlerIllegalArgumentException(IllegalArgumentException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to IllegalArgumentException: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(IllegalArgumentExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> handlerBadCredentialsException(BadCredentialsException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to BadCredentialsException: {}", message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(BadCredentialsExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handlerAuthenticationCredentialsNotFoundException(
            AuthenticationCredentialsNotFoundException e
    ) {
        String message = e.getMessage();

        log.error("Service unavailable due to AuthenticationCredentialsNotFoundException: {}", message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(
                        AuthenticationCredentialsNotFoundExceptionDto.create(message, e.getClass().toString())
                );
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handlerRuntimeException(RuntimeException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to RuntimeException: {}", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(RuntimeExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handlerUsernameNotFoundException(UsernameNotFoundException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to UsernameNotFoundException: {}", message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(UsernameNotFoundExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handlerDataIntegrityViolationException(DataIntegrityViolationException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to DataIntegrityViolationException: {}", message);

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(DataIntegrityViolationExceptionDto.create(message, e.getClass().toString()));
    }


    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handlerNoHandlerFoundException(NoHandlerFoundException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to NoHandlerFoundException: {}", message);

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(NoHandlerFoundExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(MalformedJwtException.class)
    public ResponseEntity<?> handlerExpiredJwtException(MalformedJwtException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to MalformedJwtException: {}", message);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(MalformedJwtExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handlerExpiredJwtException(ExpiredJwtException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to ExpiredJwtException: {}", message);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(ExpiredJwtExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handlerExpiredJwtException(SignatureException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to SignatureException: {}", message);

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(SignatureExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(InvalidResetTokenException.class)
    public ResponseEntity<?> handlerInvalidResetTokenException(InvalidResetTokenException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to InvalidResetTokenException: {}", message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(InvalidResetTokenExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handlerAccessDeniedException(AccessDeniedException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to AccessDeniedException: {}", message);

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(AccessDeniedExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(AuthenticationEntryPointException.class)
    public ResponseEntity<?> handlerAuthenticationEntryPointException(AuthenticationEntryPointException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to AuthenticationEntryPointException: {}", message);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(AuthenticationEntryPointExceptionDto.create(message, e.getClass().toString()));
    }

    @ExceptionHandler(LogoutHandlerException.class)
    public ResponseEntity<?> handlerLogoutHandlerException(LogoutHandlerException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to LogoutHandlerException: {}", message);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(LogoutHandlerExceptionDto.create(message, e.getClass().toString()));
    }


    //Обработка BindingResult

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handlerMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getMessage();

        log.error("Service unavailable due to MethodArgumentNotValidException: {}", message);

        Map<String, String> errors = e.getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Objects.requireNonNullElse(
                                fieldError.getDefaultMessage(), "Invalid field"
                        ),
                        (existing, replacement) -> existing // При дубликатах берем первое сообщение
                ));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(MethodArgumentNotValidExceptionDto.create("Request validation failed", errors));
    }

}
