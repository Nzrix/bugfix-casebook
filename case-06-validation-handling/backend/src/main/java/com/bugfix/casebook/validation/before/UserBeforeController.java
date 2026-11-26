package com.bugfix.casebook.validation.before;

import com.bugfix.casebook.validation.UserDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * BEFORE: No validation, returns 500 errors
 * ไม่มีการตรวจสอบความถูกต้อง ส่งกลับ 500 errors
 */
@RestController
@RequestMapping("/api/before/users")
public class UserBeforeController {
    
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDTO user) {
        // ❌ BUG 1: No @Valid annotation - validation doesn't happen
        
        // ❌ BUG 2: Business logic throws uncaught exception
        if (user.email() == null || !user.email().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
            // This becomes a 500 Internal Server Error!
        }
        
        // ❌ BUG 3: Another uncaught exception
        if (user.password() != null && user.password().length() < 8) {
            throw new RuntimeException("Password too short");
            // Also becomes a 500 error with stack trace!
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("user", user);
        return ResponseEntity.ok(response);
    }
    
    // ❌ BUG: No exception handler - all exceptions become 500 errors
    // Stack traces are exposed to clients
}
