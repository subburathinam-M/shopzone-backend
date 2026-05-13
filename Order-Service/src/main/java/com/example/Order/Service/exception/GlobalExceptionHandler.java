package com.example.Order.Service.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<Object> handleProductNotFound(ProductNotFoundException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Product Not Found");
        body.put("message", ex.getMessage());
        body.put("success", false);
        
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // 🔥 NEW: Handle out of stock
    @ExceptionHandler(OutOfStockException.class)
    public ResponseEntity<Object> handleOutOfStock(OutOfStockException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Out of Stock");
        body.put("message", ex.getMessage());
        body.put("success", false);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
    
     // 🔥 NEW: Handle invalid quantity
     @ExceptionHandler(InvalidQuantityException.class)
     public ResponseEntity<Object> handleInvalidQuantity(InvalidQuantityException ex) {
         Map<String, Object> body = new LinkedHashMap<>();
         body.put("timestamp", LocalDateTime.now());
         body.put("status", HttpStatus.BAD_REQUEST.value());
         body.put("error", "Invalid Quantity");
         body.put("message", ex.getMessage());
         body.put("success", false);
         return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
     }
     
    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Object> handleServiceUnavailable(ServiceUnavailableException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.SERVICE_UNAVAILABLE.value());
        body.put("error", "Service Unavailable");
        body.put("message", ex.getMessage());
        body.put("success", false);
        
        return new ResponseEntity<>(body, HttpStatus.SERVICE_UNAVAILABLE);
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("success", false);
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}