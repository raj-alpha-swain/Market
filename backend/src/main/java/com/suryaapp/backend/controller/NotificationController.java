package com.suryaapp.backend.controller;

import com.suryaapp.backend.dto.NotificationRequest;
import com.suryaapp.backend.dto.NotificationResponse;
import com.suryaapp.backend.service.NotificationService;
import com.suryaapp.backend.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount() {
        // For now, return all notifications count
        // In future, implement read/unread tracking
        long count = notificationService.getAllNotifications().size();
        return ResponseEntity.ok(count);
    }

    @PostMapping
    public ResponseEntity<?> createNotification(
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            String username = extractUsername(authHeader);
            NotificationResponse created = notificationService.createNotification(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNotification(
            @PathVariable Long id,
            @Valid @RequestBody NotificationRequest request,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            validateAdmin(authHeader);
            NotificationResponse updated = notificationService.updateNotification(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(
            @PathVariable Long id,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            validateAdmin(authHeader);
            notificationService.deleteNotification(id);
            return ResponseEntity.ok(new SuccessResponse("Notification deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse(e.getMessage()));
        }
    }

    private String extractUsername(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractUsername(token);
    }

    private void validateAdmin(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid authorization header");
        }
        String token = authHeader.substring(7);
        String role = jwtUtil.extractRole(token);
        if (!"ADMIN".equals(role)) {
            throw new RuntimeException("Admin access required");
        }
    }
}

class SuccessResponse {
    private String message;

    public SuccessResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
