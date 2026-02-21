package com.suryaapp.backend.service;

import com.suryaapp.backend.dto.NotificationRequest;
import com.suryaapp.backend.dto.NotificationResponse;
import com.suryaapp.backend.entity.Notification;
import com.suryaapp.backend.entity.User;
import com.suryaapp.backend.repository.NotificationRepository;
import com.suryaapp.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public List<NotificationResponse> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request, String username) {
        User admin = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification notification = new Notification();
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType() != null ? request.getType() : "INFO");
        notification.setCreatedBy(admin);

        Notification saved = notificationRepository.save(notification);
        return toResponse(saved);
    }

    @Transactional
    public NotificationResponse updateNotification(Long id, NotificationRequest request) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType() != null ? request.getType() : notification.getType());

        Notification updated = notificationRepository.save(notification);
        return toResponse(updated);
    }

    @Transactional
    public void deleteNotification(Long id) {
        if (!notificationRepository.existsById(id)) {
            throw new RuntimeException("Notification not found");
        }
        notificationRepository.deleteById(id);
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.getCreatedBy() != null ? notification.getCreatedBy().getUsername() : "System",
                notification.getCreatedAt(),
                notification.getUpdatedAt());
    }
}
