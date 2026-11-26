package com.bugfix.casebook.validation.after;

import com.bugfix.casebook.validation.UserDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * AFTER: Proper validation with @Valid
 * การตรวจสอบความถูกต้องอย่างเหมาะสมด้วย @Valid
 */
@RestController
@RequestMapping("/api/after/users")
public class UserAfterController {
    
    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO user) {
        // ✅ FIX 1: @Valid annotation triggers validation
        // Validation failures automatically return 400 Bad Request
        
        // ✅ FIX 2: Business logic exceptions handled by GlobalExceptionHandler
        if (isDuplicateEmail(user.email())) {
            throw new DuplicateEmailException("Email already exists: " + user.email());
            // This will be caught and return 409 Conflict
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("user", user);
        return ResponseEntity.ok(response);
    }
    
    private boolean isDuplicateEmail(String email) {
        // Simulate database check
        return email.equals("existing@example.com");
    }
}

class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
