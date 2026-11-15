package com.civicbuild.service.impl;

import com.civicbuild.entity.Notification;
import com.civicbuild.enums.NotificationType;
import com.civicbuild.repository.NotificationRepository;
import com.civicbuild.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification createNotification(String recipientEmail, String title, String message, NotificationType type) {
        log.info("[MOCK EMAIL ADAPTER] Dispatching email to: {} | Subject: {} | Body: {}", recipientEmail, title, message);
        Notification notification = new Notification(recipientEmail, title, message, type);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotificationsForUser(String email) {
        return notificationRepository.findByRecipientEmailOrderBySentAtDesc(email);
    }

    @Override
    @Transactional
    public void markAsRead(Long id) {
        notificationRepository.findById(id).ifPresent(n -> {
            n.setReadStatus(true);
            notificationRepository.save(n);
        });
    }
}
