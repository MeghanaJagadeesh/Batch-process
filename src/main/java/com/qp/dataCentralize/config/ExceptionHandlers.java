package com.qp.dataCentralize.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlers extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleJWTException(Exception e) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("message", e.getMessage());
        map.put("code", 500);
        map.put("status", "fail");
        map.put("data", e);
        return ResponseEntity.internalServerError().body(map);
    }
}
