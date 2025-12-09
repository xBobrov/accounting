package com.vodokanal.accounting.util;

import com.vodokanal.accounting.dto.ErrorResponseDto;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class ExceptionHandlerUtil {

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDto> handConstraintViolationException(ConstraintViolationException e){

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", e.getMessage());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponseDto> handleHMVException(HandlerMethodValidationException e){

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", "В переданном списке содержаться ошибки");

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        ErrorResponseDto errorResponseDto = new ErrorResponseDto(LocalDateTime.now(),
                "Bad Request", getErrorMessageList(e).toString());

        return new ResponseEntity<>(errorResponseDto, HttpStatus.BAD_REQUEST);
    }

    private List<String> getErrorMessageList(MethodArgumentNotValidException e) {

       return e.getBindingResult().getAllErrors().stream()
               .map(DefaultMessageSourceResolvable::getDefaultMessage)
               .toList();
    }
}
