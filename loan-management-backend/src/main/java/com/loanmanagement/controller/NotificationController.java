package com.loanmanagement.controller;

import com.loanmanagement.dto.response.ApiResponse;
import com.loanmanagement.entity.Notification;
import com.loanmanagement.entity.User;
import com.loanmanagement.service.NotificationService;
import com.loanmanagement.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for managing user notifications
 */
@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUserNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        List<Notification> notifications = notificationService.getUserNotifications(user.getId());

        List<Map<String, Object>> response = notifications.stream()
                .map(this::mapNotification)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Notifications retrieved")
                .data(response)
                .build());
    }

    /**
     * Get unread notifications
     */
    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getUnreadNotifications(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        List<Notification> notifications = notificationService.getUnreadNotifications(user.getId());

        List<Map<String, Object>> response = notifications.stream()
                .map(this::mapNotification)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<Map<String, Object>>>builder()
                .success(true)
                .message("Unread notifications retrieved")
                .data(response)
                .build());
    }

    /**
     * Get unread notification count
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getUnreadCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        long count = notificationService.countUnreadNotifications(user.getId());

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Unread count retrieved")
                .data(count)
                .build());
    }

    /**
     * Mark notification as read
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Map<String, Object>>> markAsRead(@PathVariable Long id) {
        Notification notification = notificationService.markAsRead(id);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Notification marked as read")
                .data(mapNotification(notification))
                .build());
    }

    /**
     * Mark all notifications as read
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Integer>> markAllAsRead(
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());
        int count = notificationService.markAllAsRead(user.getId());

        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(true)
                .message("All notifications marked as read")
                .data(count)
                .build());
    }

    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteNotification(@PathVariable Long id) {
        notificationService.deleteNotification(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Notification deleted")
                .build());
    }

    private Map<String, Object> mapNotification(Notification n) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", n.getId());
        map.put("type", n.getType().name());
        map.put("title", n.getTitle());
        map.put("message", n.getMessage());
        map.put("isRead", n.isRead());
        map.put("createdAt", n.getCreatedAt());
        map.put("readAt", n.getReadAt());
        map.put("relatedEntityType", n.getRelatedEntityType());
        map.put("relatedEntityId", n.getRelatedEntityId());
        map.put("actionUrl", n.getActionUrl());
        return map;
    }
}
