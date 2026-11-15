package com.civicbuild.service;

import com.civicbuild.entity.Notification;
import com.civicbuild.enums.NotificationType;
import java.util.List;

public interface NotificationService {
    Notification createNotification(String recipientEmail, String title, String message, NotificationType type);
    List<Notification> getNotificationsForUser(String email);
    void markAsRead(Long id);
}
