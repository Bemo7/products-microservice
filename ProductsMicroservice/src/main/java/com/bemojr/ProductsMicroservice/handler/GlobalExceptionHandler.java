package com.bemojr.ProductsMicroservice.handler;

import com.bemojr.ProductsMicroservice.dto.ResponseMsg;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleException(Exception e, HttpServletRequest request){
        log.error("Server ran into an issue {} from path {}" ,e.getMessage(), request.getServletPath());
        ResponseMsg responseMsg = ResponseMsg.builder()
                .status("fail")
                .message(e.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseMsg);
    }
}
