package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.ErrorResponseDto;
import com.vodokanal.accounting.exception.DataAlreadyExistsException;
import com.vodokanal.accounting.exception.DataNotFoundException;
import org.hibernate.exception.ConstraintViolationException;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", e.getMessage());
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DataNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleDataNotFoundException(DataNotFoundException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", e.getMessage());
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(DataAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleDataAlreadyExistsException(DataAlreadyExistsException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Conflict", e.getMessage());
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.CONFLICT);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handleConstraintViolationException(ConstraintViolationException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", e.getMessage());
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleHMVException(HandlerMethodValidationException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", "В переданном списке содержаться ошибки");
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", getErrorMessageList(e).toString());
        log.error("При записи в базу данных произошла ошибка: {}", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    private List<String> getErrorMessageList(MethodArgumentNotValidException e) {

        return e.getBindingResult().getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .toList();
    }
}
