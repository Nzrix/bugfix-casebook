package com.bugfix.casebook.validation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ValidationTest {
    
    @LocalServerPort
    private int port;
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void beforeVersion_Returns500ForValidationErrors() {
        System.out.println("=== BEFORE: Testing validation errors ===");
        
        String url = "http://localhost:" + port + "/api/before/users";
        String invalidJson = "{\"name\":\"John\",\"email\":\"invalid-email\",\"password\":\"123\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(invalidJson, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // ❌ BUG: Returns 500 instead of 400
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        // ❌ BUG: Response may contain stack trace
        assertNotNull(response.getBody());
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_Returns400ForValidationErrors() {
        System.out.println("=== AFTER: Testing validation errors ===");
        
        String url = "http://localhost:" + port + "/api/after/users";
        String invalidJson = "{\"name\":\"A\",\"email\":\"invalid-email\",\"password\":\"123\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(invalidJson, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // ✅ FIX: Returns 400 Bad Request
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        // ✅ FIX: Response contains validation errors, no stack trace
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("Validation Failed"));
        assertTrue(body.contains("email"));
        assertFalse(body.contains("java.lang")); // No stack trace
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_Returns409ForDuplicateEmail() {
        System.out.println("=== AFTER: Testing duplicate email ===");
        
        String url = "http://localhost:" + port + "/api/after/users";
        String json = "{\"name\":\"John Doe\",\"email\":\"existing@example.com\",\"password\":\"password123\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // ✅ FIX: Returns 409 Conflict for business logic error
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("already exists"));
        
        System.out.println("=========================================");
    }
    
    @Test
    void afterVersion_Returns200ForValidInput() {
        System.out.println("=== AFTER: Testing valid input ===");
        
        String url = "http://localhost:" + port + "/api/after/users";
        String json = "{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}";
        
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<String> request = new HttpEntity<>(json, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
        
        System.out.println("Status: " + response.getStatusCode());
        System.out.println("Body: " + response.getBody());
        
        // ✅ Success case
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        String body = response.getBody();
        assertNotNull(body);
        assertTrue(body.contains("created successfully"));
        
        System.out.println("=========================================");
    }
}
