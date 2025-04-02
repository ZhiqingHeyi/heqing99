package com.hotel.controller;

import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {
    
    private final ErrorAttributes errorAttributes;
    
    public CustomErrorController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request, WebRequest webRequest) {
        HttpStatus status = getStatus(request);
        
        if (status == HttpStatus.NOT_FOUND) {
            // 对于404错误，可能是前端路由，返回简单响应
            Map<String, Object> body = new HashMap<>();
            body.put("status", 404);
            body.put("message", "Not Found");
            return new ResponseEntity<>(body, HttpStatus.OK);
        }
        
        // 构建简单的错误响应
        Map<String, Object> body = new HashMap<>();
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", "An error occurred");
        
        return new ResponseEntity<>(body, status);
    }
    
    private HttpStatus getStatus(HttpServletRequest request) {
        Integer statusCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        if (statusCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (Exception ex) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
} 